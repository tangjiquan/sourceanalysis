
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#ifndef _NGX_BUF_H_INCLUDED_
#define _NGX_BUF_H_INCLUDED_


#include <ngx_config.h>
#include <ngx_core.h>


typedef void *            ngx_buf_tag_t;

typedef struct ngx_buf_s  ngx_buf_t;

struct ngx_buf_s {
	//pos告诉使用者本次应该从pos位置开始处理内存中的数据，这样设置的原因是一个ngx_buf_t可以被多次反复处理，
    u_char          *pos;
    //last表示有效内容到此为止，注意：pos和last之间的内存是希望nginx处理的内容
    u_char          *last;
    //file_pos表示将要处理的文件位置，fil_last表示截止的文件位置
    off_t            file_pos;
    off_t            file_last;

    //如果ngx_buf_t缓冲区用于内存，	那么start指向这段内存的起始地址，end指向这段内存的末尾
    u_char          *start;         /* start of buffer */
    u_char          *end;           /* end of buffer */


    ngx_buf_tag_t    tag;
    //引用的文件
    ngx_file_t      *file;

    ngx_buf_t       *shadow;


    /* the buf's content could be changed */
    unsigned         temporary:1;//临时内存标志位，为1时表示内存中这段内存可以修改

    /*
     * the buf's content is in a memory cache or in a read only memory
     * and must not be changed
     */
    unsigned         memory:1;//内存标志位，为1时表示内存中这段内存不可以修改

    /* the buf's content is mmap()ed and must not be changed */
    unsigned         mmap:1;//为1时，表示这段内存是用mmap系统调用映射过来的，不可以被修改

    unsigned         recycled:1;//标志位，为1时表示可以回收
    //当nginx检测到是1的时候，会从ngx_buf_t中file成员处获取实际的文件
    unsigned         in_file:1;//为1时表示这段缓冲区处理的是文件而不是内存
    unsigned         flush:1;//为1时，需要执行flush
    unsigned         sync:1;//
    unsigned         last_buf:1;//最后一块待处理的缓冲区
    unsigned         last_in_chain:1;//表示是否是ngx_chain_t中的最后一个缓冲区

    unsigned         last_shadow:1;
    unsigned         temp_file:1;//是否是临时文件

    /* STUB */ int   num;
};


struct ngx_chain_s {
    ngx_buf_t    *buf;
    ngx_chain_t  *next;
};


typedef struct {
    ngx_int_t    num;
    size_t       size;
} ngx_bufs_t;


typedef struct ngx_output_chain_ctx_s  ngx_output_chain_ctx_t;

typedef ngx_int_t (*ngx_output_chain_filter_pt)(void *ctx, ngx_chain_t *in);

#if (NGX_HAVE_FILE_AIO)
typedef void (*ngx_output_chain_aio_pt)(ngx_output_chain_ctx_t *ctx,
    ngx_file_t *file);
#endif

struct ngx_output_chain_ctx_s {
    ngx_buf_t                   *buf;
    ngx_chain_t                 *in;
    ngx_chain_t                 *free;
    ngx_chain_t                 *busy;

    unsigned                     sendfile:1;
    unsigned                     directio:1;
#if (NGX_HAVE_ALIGNED_DIRECTIO)
    unsigned                     unaligned:1;
#endif
    unsigned                     need_in_memory:1;
    unsigned                     need_in_temp:1;
#if (NGX_HAVE_FILE_AIO)
    unsigned                     aio:1;

    ngx_output_chain_aio_pt      aio_handler;
#endif

    off_t                        alignment;

    ngx_pool_t                  *pool;
    ngx_int_t                    allocated;
    ngx_bufs_t                   bufs;
    ngx_buf_tag_t                tag;

    ngx_output_chain_filter_pt   output_filter;
    void                        *filter_ctx;
};


typedef struct {
    ngx_chain_t                 *out;
    ngx_chain_t                **last;
    ngx_connection_t            *connection;
    ngx_pool_t                  *pool;
    off_t                        limit;
} ngx_chain_writer_ctx_t;


#define NGX_CHAIN_ERROR     (ngx_chain_t *) NGX_ERROR


#define ngx_buf_in_memory(b)        (b->temporary || b->memory || b->mmap)
#define ngx_buf_in_memory_only(b)   (ngx_buf_in_memory(b) && !b->in_file)

#define ngx_buf_special(b)                                                   \
    ((b->flush || b->last_buf || b->sync)                                    \
     && !ngx_buf_in_memory(b) && !b->in_file)

#define ngx_buf_sync_only(b)                                                 \
    (b->sync                                                                 \
     && !ngx_buf_in_memory(b) && !b->in_file && !b->flush && !b->last_buf)

#define ngx_buf_size(b)                                                      \
    (ngx_buf_in_memory(b) ? (off_t) (b->last - b->pos):                      \
                            (b->file_last - b->file_pos))

ngx_buf_t *ngx_create_temp_buf(ngx_pool_t *pool, size_t size);
ngx_chain_t *ngx_create_chain_of_bufs(ngx_pool_t *pool, ngx_bufs_t *bufs);


#define ngx_alloc_buf(pool)  ngx_palloc(pool, sizeof(ngx_buf_t))
#define ngx_calloc_buf(pool) ngx_pcalloc(pool, sizeof(ngx_buf_t))

ngx_chain_t *ngx_alloc_chain_link(ngx_pool_t *pool);
#define ngx_free_chain(pool, cl)                                             \
    cl->next = pool->chain;                                                  \
    pool->chain = cl



ngx_int_t ngx_output_chain(ngx_output_chain_ctx_t *ctx, ngx_chain_t *in);
ngx_int_t ngx_chain_writer(void *ctx, ngx_chain_t *in);

ngx_int_t ngx_chain_add_copy(ngx_pool_t *pool, ngx_chain_t **chain,
    ngx_chain_t *in);
ngx_chain_t *ngx_chain_get_free_buf(ngx_pool_t *p, ngx_chain_t **free);
void ngx_chain_update_chains(ngx_pool_t *p, ngx_chain_t **free,
    ngx_chain_t **busy, ngx_chain_t **out, ngx_buf_tag_t tag);


#endif /* _NGX_BUF_H_INCLUDED_ */
