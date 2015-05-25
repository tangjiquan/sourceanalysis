
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#ifndef _NGX_ARRAY_H_INCLUDED_
#define _NGX_ARRAY_H_INCLUDED_


#include <ngx_config.h>
#include <ngx_core.h>

//ngx_array_t是顺序容器，当到达数组容量上限时候动态改变数组的大小，类似stl中的vector，能够动态增长
//由slab内存池统一管理分配出的内存，效率高
typedef struct {
    void        *elts;	//elts指向数组的首地址
    ngx_uint_t   nelts;	//netls是数组中已经使用的元素的个数
    size_t       size;	//每个数组元素占用内存大小
    ngx_uint_t   nalloc;//当前数组中能够容纳元素的总大小
    ngx_pool_t  *pool;	//内存池对象
} ngx_array_t;


ngx_array_t *ngx_array_create(ngx_pool_t *p, ngx_uint_t n, size_t size);
void ngx_array_destroy(ngx_array_t *a);
void *ngx_array_push(ngx_array_t *a);
void *ngx_array_push_n(ngx_array_t *a, ngx_uint_t n);


static ngx_inline ngx_int_t
ngx_array_init(ngx_array_t *array, ngx_pool_t *pool, ngx_uint_t n, size_t size)
{
    /*
     * set "array->nelts" before "array->elts", otherwise MSVC thinks
     * that "array->nelts" may be used without having been initialized
     */

    array->nelts = 0;
    array->size = size;
    array->nalloc = n;
    array->pool = pool;
    //分配n个大小为size的内存，elts指向首地址
    array->elts = ngx_palloc(pool, n * size);
    if (array->elts == NULL) {
        return NGX_ERROR;
    }

    return NGX_OK;
}


#endif /* _NGX_ARRAY_H_INCLUDED_ */
