
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#include <ngx_config.h>
#include <ngx_core.h>

/**
 * 创建新的链表
 * pool:内存池对象
 * size:每一个元素的大小
 * n:每一个链表数组可以容纳元素的个数
 */
ngx_list_t *
ngx_list_create(ngx_pool_t *pool, ngx_uint_t n, size_t size)
{
    ngx_list_t  *list;

    list = ngx_palloc(pool, sizeof(ngx_list_t));
    if (list == NULL) {
        return NULL;
    }

    if (ngx_list_init(list, pool, n, size) != NGX_OK) {
        return NULL;
    }

    return list;
}

/**
 * 添加新的元素
 */
void *
ngx_list_push(ngx_list_t *l)
{
    void             *elt;
    ngx_list_part_t  *last;

    last = l->last;

    if (last->nelts == l->nalloc) {

        /* the last part is full, allocate a new list part */

        last = ngx_palloc(l->pool, sizeof(ngx_list_part_t));
        if (last == NULL) {
            return NULL;
        }

        last->elts = ngx_palloc(l->pool, l->nalloc * l->size);
        if (last->elts == NULL) {
            return NULL;
        }

        last->nelts = 0;
        last->next = NULL;

        l->last->next = last;
        l->last = last;
    }

    elt = (char *) last->elts + l->size * last->nelts;
    last->nelts++;

    return elt;
}


/**
 * 遍历链表中的元素
 *


void ngx_select (ngx_list_t testlist){
	int i;
	ngx_list_part_t * part = *testlist;
	ngx_str_t* str = part->elts;
	for(i=0; ; i++){
		if(i>part->nelts){
			//如果某个ngx_list_part_t数组的next指针为空
			//则说明已经遍历完数组
			if(part->next==NULL){
				break;
			}
		}
		//访问下一个ngx_list_part_t
		part=part->next;
		header=part->elts;

		//将i序号置为0，准备重新访问下一个数组
		i=0;
	}
	//这里方便的取出当前遍历到的链表元素
	printf("list elememt:%*s\n",str[i].len, str[i].data);
}


*/
