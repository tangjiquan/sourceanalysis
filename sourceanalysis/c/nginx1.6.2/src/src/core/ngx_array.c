
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#include <ngx_config.h>
#include <ngx_core.h>

//p为内存池，n为初始化分配分配元素的个数，size为每个元素占内存的大小
ngx_array_t *
ngx_array_create(ngx_pool_t *p, ngx_uint_t n, size_t size)
{
    ngx_array_t *a;

    //分配动态数组指针
    a = ngx_palloc(p, sizeof(ngx_array_t));
    if (a == NULL) {
        return NULL;
    }

    if (ngx_array_init(a, p, n, size) != NGX_OK) {
        return NULL;
    }

    return a;
}


void
ngx_array_destroy(ngx_array_t *a)
{
    ngx_pool_t  *p;

    p = a->pool;

    //释放动态数组每个元素
    if ((u_char *) a->elts + a->size * a->nalloc == p->d.last) {
        p->d.last -= a->size * a->nalloc;
    }

    //释放动态数组首指针
    if ((u_char *) a + sizeof(ngx_array_t) == p->d.last) {
        p->d.last = (u_char *) a;
    }
}

//a为要添加到动态数组的指针
void *
ngx_array_push(ngx_array_t *a)
{
    void        *elt, *new;
    size_t       size;
    ngx_pool_t  *p;

    //使用和预先分配的个数相等，数组已满
    if (a->nelts == a->nalloc) {

        /* the array is full */
    	  //再分配预分配nalloc个，现在就有2*nalloc个了
        size = a->size * a->nalloc;

        p = a->pool;
        //如果内存池内存还够，直接从内存池分配，只分配一个
        if ((u_char *) a->elts + size == p->d.last
            && p->d.last + a->size <= p->d.end)
        {
            /*
             * the array allocation is the last in the pool
             * and there is space for new allocation
             */
        	//内存池尾指针后移一个元素大小，分配内存一个元素，并把nalloc+1
            p->d.last += a->size;
            a->nalloc++;

        //如果内存池内存不够了，分配一个新的数组，大小为两倍的nalloc
        } else {
            /* allocate a new array */

        	 //内存分配
            new = ngx_palloc(p, 2 * size);
            if (new == NULL) {
                return NULL;
            }

            //将以前的数组拷贝到新数组，并将数组大小设置为以前二倍
            ngx_memcpy(new, a->elts, size);
            a->elts = new;
            a->nalloc *= 2;
        }
    }

    //已分配个数+1 ，并返回之
    elt = (u_char *) a->elts + a->size * a->nelts;
    a->nelts++;

    return elt;
}
// 可以看到，当内存池有空间时，数组满后仅增加一个元素，当内存池没有未分配空间时，直接分配2*nalloc 个大小

//a为要放入的数组，n为要放入的个数
void *
ngx_array_push_n(ngx_array_t *a, ngx_uint_t n)
{
    void        *elt, *new;
    size_t       size;
    ngx_uint_t   nalloc;
    ngx_pool_t  *p;

    size = n * a->size;
    //如果数组剩余个数不够分配
    if (a->nelts + n > a->nalloc) {

        /* the array is full */

        p = a->pool;

        //如果内存池中剩余的够分配n个元素，从内存池中分配
        if ((u_char *) a->elts + a->size * a->nalloc == p->d.last
            && p->d.last + size <= p->d.end)
        {
            /*
             * the array allocation is the last in the pool
             * and there is space for new allocation
             */

            p->d.last += size;
            a->nalloc += n;

            //如果内存池中剩余的不够分配n个元素
        } else {
            /* allocate a new array */
        	 //当n比数组预分配个数nalloc大时，分配2n个，否则分配2*nalloc个
            nalloc = 2 * ((n >= a->nalloc) ? n : a->nalloc);

            new = ngx_palloc(p, nalloc * a->size);
            if (new == NULL) {
                return NULL;
            }
            //拷贝以前元素，设置nalloc
            ngx_memcpy(new, a->elts, a->nelts * a->size);
            a->elts = new;
            a->nalloc = nalloc;
        }
    }
    //增加已分配个数，并返回
    elt = (u_char *) a->elts + a->size * a->nelts;
    a->nelts += n;

    return elt;
}
