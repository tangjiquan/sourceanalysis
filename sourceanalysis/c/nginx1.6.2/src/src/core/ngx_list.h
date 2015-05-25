
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#ifndef _NGX_LIST_H_INCLUDED_
#define _NGX_LIST_H_INCLUDED_


#include <ngx_config.h>
#include <ngx_core.h>

//ngx_list_part_t只描述链表的一个元素
typedef struct ngx_list_part_s  ngx_list_part_t;

struct ngx_list_part_s {
    void             *elts;//指向数组的起始地址
    ngx_uint_t        nelts;//表示数组中已经使用的元素，nelts必须小于ngx_list_t结构体中的nalloc
    ngx_list_part_t  *next;//下一个链表元素ngx_list_part_t的地址
};

//ngx_list_t描述整个链表，起始ngx_list_t是一个链表容器，而链表中的元素又是一个数组
//ngx_list_t链表中能够容纳元素的个数是由ngx_list_part_t数组元素的个数与每一个数组所容纳的元素相乘
typedef struct {
    ngx_list_part_t  *last;//指向链表的最后一个数组元素
    ngx_list_part_t   part;//链表的首个数组元素
    size_t            size;//每一个数组元素占用的空间大小
    ngx_uint_t        nalloc;//链表的数组元素一旦分配后是不可以更改的，nalloc表示每一个ngx_list_part_t数组的容量，
    ngx_pool_t       *pool;//链表中管理内存分配的内存池对象，用户要存放的数据占用的内存都是有ngx_pool_t分配的
} ngx_list_t;


ngx_list_t *ngx_list_create(ngx_pool_t *pool, ngx_uint_t n, size_t size);

/*
 * 初始化一个已有的链表
 */
static ngx_inline ngx_int_t
ngx_list_init(ngx_list_t *list, ngx_pool_t *pool, ngx_uint_t n, size_t size)
{
    list->part.elts = ngx_palloc(pool, n * size);
    if (list->part.elts == NULL) {
        return NGX_ERROR;
    }

    list->part.nelts = 0;
    list->part.next = NULL;
    list->last = &list->part;
    list->size = size;
    list->nalloc = n;
    list->pool = pool;

    return NGX_OK;
}


/*
 *
 *  the iteration through the list:
 *
 *  part = &list.part;
 *  data = part->elts;
 *
 *  for (i = 0 ;; i++) {
 *
 *      if (i >= part->nelts) {
 *          if (part->next == NULL) {
 *              break;
 *          }
 *
 *          part = part->next;
 *          data = part->elts;
 *          i = 0;
 *      }
 *
 *      ...  data[i] ...
 *
 *  }
 */


void *ngx_list_push(ngx_list_t *list);


#endif /* _NGX_LIST_H_INCLUDED_ */
