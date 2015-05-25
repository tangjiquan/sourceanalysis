
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#include <ngx_config.h>
#include <ngx_core.h>


#if (NGX_HAVE_ATOMIC_OPS)


static void ngx_shmtx_wakeup(ngx_shmtx_t *mtx);


ngx_int_t
ngx_shmtx_create(ngx_shmtx_t *mtx, ngx_shmtx_sh_t *addr, u_char *name)
{
    mtx->lock = &addr->lock;

    //当spin值为-1时，表示不能使用信号量，这时直接返回成功
    if (mtx->spin == (ngx_uint_t) -1) {
        return NGX_OK;
    }

    //spin值默认为2048
    mtx->spin = 2048;

    //同时使用信号量
#if (NGX_HAVE_POSIX_SEM)

    mtx->wait = &addr->wait;

    //以多进程使用的方式初始化sem信号量
    //信号量mtx->sem初始化为0，用于进程间通信
    if (sem_init(&mtx->sem, 1, 0) == -1) {
        ngx_log_error(NGX_LOG_ALERT, ngx_cycle->log, ngx_errno,
                      "sem_init() failed");
    } else {
    	//在信号量初始化成功后，设置semaphore标志位为1
        mtx->semaphore = 1;
    }

#endif

    return NGX_OK;
}


void
ngx_shmtx_destroy(ngx_shmtx_t *mtx)
{
	//支持信号量时才有代码需要执行
#if (NGX_HAVE_POSIX_SEM)

	//当这把锁的spin值不为（ngx_unit_t)-1时，且初始化信号量成功，semaphore标志位才为1
	//关闭ngx_shmtx_t结构体中的fd句柄
    if (mtx->semaphore) {
    	//销毁信号量
        if (sem_destroy(&mtx->sem) == -1) {
            ngx_log_error(NGX_LOG_ALERT, ngx_cycle->log, ngx_errno,
                          "sem_destroy() failed");
        }
    }

#endif
}


ngx_uint_t
ngx_shmtx_trylock(ngx_shmtx_t *mtx)
{
    return (*mtx->lock == 0 && ngx_atomic_cmp_set(mtx->lock, 0, ngx_pid));
}


void
ngx_shmtx_lock(ngx_shmtx_t *mtx)
{
    ngx_uint_t         i, n;

    ngx_log_debug0(NGX_LOG_DEBUG_CORE, ngx_cycle->log, 0, "shmtx lock");

    //没有拿到锁之前是不会跳出循环的
    for ( ;; ) {

        if (*mtx->lock == 0 && ngx_atomic_cmp_set(mtx->lock, 0, ngx_pid)) {
            return;
        }

        //仅在多处理器状态下spin值才有意义，否则PAUSE指令是不会执行的
        if (ngx_ncpu > 1) {

        	//循环执行PAUSE，检查锁是否已经释放
            for (n = 1; n < mtx->spin; n <<= 1) {

            	//随着长时间没有获得锁，将会执行更多次PAUSE才会检查锁
                for (i = 0; i < n; i++) {
                	//对于对处理器系统，执行ngx_cpu_pause可以降低功耗
                    ngx_cpu_pause();
                }

                //检查lock是否已经为非负数，即锁是否已经释放，如果锁已经释放，那么会通过将lock
                //原子变量值设置为负数来表示当前进程持有了锁
                if (*mtx->lock == 0
                    && ngx_atomic_cmp_set(mtx->lock, 0, ngx_pid))
                {
                    return;
                }
            }
        }

        //支持信号量时才继续执行
#if (NGX_HAVE_POSIX_SEM)

        //semaphore标志位为1才使用信号量
        if (mtx->semaphore) {
            (void) ngx_atomic_fetch_add(mtx->wait, 1);

            if (*mtx->lock == 0 && ngx_atomic_cmp_set(mtx->lock, 0, ngx_pid)) {
                return;
            }

            ngx_log_debug1(NGX_LOG_DEBUG_CORE, ngx_cycle->log, 0,
                           "shmtx wait %uA", *mtx->wait);

            //如果没有拿到锁，这时nginx进程将会睡眠，直到其他进程释放了锁
            //检查信号量sem的值，如果sem值为正数，则sem值减1，表示拿到了信号量互斥锁，同时sem_wait方法返回0,。
            //如果sem值为0或者负数，则当前进程进入睡眠状态，等待其他进程使用ngx_shmtx_unlock方法释放锁（等待sem
            //信号量变为正数），到时Linux内核会重新调度当前进度，继续坚持sem值是否为正，重复以上流程
            while (sem_wait(&mtx->sem) == -1) {
                ngx_err_t  err;

                err = ngx_errno;

                //当EINTR信号出现时，表示sem_wait只是被打断，并不是出错
                if (err != NGX_EINTR) {
                    ngx_log_error(NGX_LOG_ALERT, ngx_cycle->log, err,
                                  "sem_wait() failed while waiting on shmtx");
                    break;
                }
            }

            ngx_log_debug0(NGX_LOG_DEBUG_CORE, ngx_cycle->log, 0,
                           "shmtx awoke");

            //循环检查lock锁的值，当使用信号量后不会调用sched_yield
            continue;
        }

#endif

        //在不使用信号量时，调用sched_yield将会使当前进程暂时让出处理器
        ngx_sched_yield();
    }
}


void
ngx_shmtx_unlock(ngx_shmtx_t *mtx)
{
    if (mtx->spin != (ngx_uint_t) -1) {
        ngx_log_debug0(NGX_LOG_DEBUG_CORE, ngx_cycle->log, 0, "shmtx unlock");
    }

    if (ngx_atomic_cmp_set(mtx->lock, ngx_pid, 0)) {
        ngx_shmtx_wakeup(mtx);
    }
}


ngx_uint_t
ngx_shmtx_force_unlock(ngx_shmtx_t *mtx, ngx_pid_t pid)
{
    ngx_log_debug0(NGX_LOG_DEBUG_CORE, ngx_cycle->log, 0,
                   "shmtx forced unlock");

    if (ngx_atomic_cmp_set(mtx->lock, pid, 0)) {
        ngx_shmtx_wakeup(mtx);
        return 1;
    }

    return 0;
}


static void
ngx_shmtx_wakeup(ngx_shmtx_t *mtx)
{
#if (NGX_HAVE_POSIX_SEM)
    ngx_atomic_uint_t  wait;

    if (!mtx->semaphore) {
        return;
    }

    for ( ;; ) {

        wait = *mtx->wait;

        if (wait == 0) {
            return;
        }

        if (ngx_atomic_cmp_set(mtx->wait, wait, wait - 1)) {
            break;
        }
    }

    ngx_log_debug1(NGX_LOG_DEBUG_CORE, ngx_cycle->log, 0,
                   "shmtx wake %uA", wait);

    if (sem_post(&mtx->sem) == -1) {
        ngx_log_error(NGX_LOG_ALERT, ngx_cycle->log, ngx_errno,
                      "sem_post() failed while wake shmtx");
    }

#endif
}


#else


ngx_int_t
ngx_shmtx_create(ngx_shmtx_t *mtx, ngx_shmtx_sh_t *addr, u_char *name)
{
    if (mtx->name) {

        if (ngx_strcmp(name, mtx->name) == 0) {
            mtx->name = name;
            return NGX_OK;
        }

        ngx_shmtx_destroy(mtx);
    }

    mtx->fd = ngx_open_file(name, NGX_FILE_RDWR, NGX_FILE_CREATE_OR_OPEN,
                            NGX_FILE_DEFAULT_ACCESS);

    if (mtx->fd == NGX_INVALID_FILE) {
        ngx_log_error(NGX_LOG_EMERG, ngx_cycle->log, ngx_errno,
                      ngx_open_file_n " \"%s\" failed", name);
        return NGX_ERROR;
    }

    if (ngx_delete_file(name) == NGX_FILE_ERROR) {
        ngx_log_error(NGX_LOG_ALERT, ngx_cycle->log, ngx_errno,
                      ngx_delete_file_n " \"%s\" failed", name);
    }

    mtx->name = name;

    return NGX_OK;
}


void
ngx_shmtx_destroy(ngx_shmtx_t *mtx)
{
    if (ngx_close_file(mtx->fd) == NGX_FILE_ERROR) {
        ngx_log_error(NGX_LOG_ALERT, ngx_cycle->log, ngx_errno,
                      ngx_close_file_n " \"%s\" failed", mtx->name);
    }
}


ngx_uint_t
ngx_shmtx_trylock(ngx_shmtx_t *mtx)
{
    ngx_err_t  err;

    //ngx_trylock_fd方法实现非阻塞互斥锁的获取
    err = ngx_trylock_fd(mtx->fd);

    if (err == 0) {
        return 1;
    }

    //如果err错误码是NGX_EAGAIN，则表示现在锁已经被其他进程持有了
    if (err == NGX_EAGAIN) {
        return 0;
    }

#if __osf__ /* Tru64 UNIX */

    if (err == NGX_EACCESS) {
        return 0;
    }

#endif

    ngx_log_abort(err, ngx_trylock_fd_n " %s failed", mtx->name);

    return 0;
}


void
ngx_shmtx_lock(ngx_shmtx_t *mtx)
{
    ngx_err_t  err;

    //ngx_lock_fd方法返回0时表示成功地持有锁，返回-1时表示出现错误
    err = ngx_lock_fd(mtx->fd);

    if (err == 0) {
        return;
    }

    ngx_log_abort(err, ngx_lock_fd_n " %s failed", mtx->name);
}


void
ngx_shmtx_unlock(ngx_shmtx_t *mtx)
{
    ngx_err_t  err;

    //返回0即表示释放锁成功
    err = ngx_unlock_fd(mtx->fd);

    if (err == 0) {
        return;
    }

    ngx_log_abort(err, ngx_unlock_fd_n " %s failed", mtx->name);
}


ngx_uint_t
ngx_shmtx_force_unlock(ngx_shmtx_t *mtx, ngx_pid_t pid)
{
    return 0;
}

#endif
