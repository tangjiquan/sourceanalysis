
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#include <ngx_config.h>
#include <ngx_core.h>


void
ngx_spinlock(ngx_atomic_t *lock, ngx_atomic_int_t value, ngx_uint_t spin)
{

#if (NGX_HAVE_ATOMIC_OPS)

    ngx_uint_t  i, n;

    //无法获取锁时进程的代码将一直在这个循环中执行
    for ( ;; ) {

    	//lock为0时表示是没有被其他进程持有的，这时将lock值设置为value参数表示当前进程持有了锁
        if (*lock == 0 && ngx_atomic_cmp_set(lock, 0, value)) {
        	//获取到锁后ngx_spinlock方法才会返回
            return;
        }

        //ngx_ncpu是处理器的个数，当它大于1时表示处于多处理器系统中
        if (ngx_ncpu > 1) {

        	//在多处理器下，更好的做法是当前进程不要立刻"让出"正在使用的cpu处理器，而是等待一段时间，看看其他处理器
        	//上的进程是否会释放，这会减少进程间切换的次数
            for (n = 1; n < spin; n <<= 1) {

            	//随着等待次数越来越多，实际去检查lock是否释放的频繁会越来越小，因为检查lock值更消耗cpu
            	//而执行ngx_cpu_pause对cpu的能耗来说是很省电的
                for (i = 0; i < n; i++) {
                	//ngx_cpu_pause是在许多架构体系中专门为了自旋锁而提供的指令，它会告诉cpu现在处于自旋锁等待状态
                	//通常一些cpu会将自己置于节能状态，降低功耗，在执行ngx_cpu_pause后，当前进程没有让出正在使用的处理器
                    ngx_cpu_pause();
                }

                //检查锁是否被释放了，如果lock值为0且释放了锁后，就把它的值设为value，当前进程持有锁成功并返回
                if (*lock == 0 && ngx_atomic_cmp_set(lock, 0, value)) {
                    return;
                }
            }
        }

        //当前进程仍然处于可执行状态，但暂时“让出”处理器，使得处理器优先调度其他可执行状态的进程，这样，在进程被内核再次
        //调度时，在for循环代码中可以期望其他进程释放锁，不同的内核版本对于sched_yield系统调用的实现可能是不同的
        //但他们的目的都是暂时“让出”处理器
        ngx_sched_yield();
    }

#else

#if (NGX_THREADS)

#error ngx_spinlock() or ngx_atomic_cmp_set() are not defined !

#endif

#endif

}
