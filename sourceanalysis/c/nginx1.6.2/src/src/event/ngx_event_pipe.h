
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#ifndef _NGX_EVENT_PIPE_H_INCLUDED_
#define _NGX_EVENT_PIPE_H_INCLUDED_


#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_event.h>


typedef struct ngx_event_pipe_s  ngx_event_pipe_t;

//处理接收来自上游的包体的回调方法原型
typedef ngx_int_t (*ngx_event_pipe_input_filter_pt)(ngx_event_pipe_t *p,
                                                    ngx_buf_t *buf);
//向下游发送响应的回调方法原型
typedef ngx_int_t (*ngx_event_pipe_output_filter_pt)(void *data,
                                                     ngx_chain_t *chain);


struct ngx_event_pipe_s {
	//nginx与上游服务器间的连接
    ngx_connection_t  *upstream;
    //nginx与下游客户端之间的连接
    ngx_connection_t  *downstream;

    //直接接收来自上游服务器的缓冲区链表，这个链表中的顺序是逆序的，也就是说，链表前端的ngx_buf_t缓冲区指向的是后接收
    //到的响应，而后端的ngx_buf_t缓冲区指向的是接收到的响应，因此，free_raw_bufs链表仅在接收响应时使用
    ngx_chain_t       *free_raw_bufs;
    //表示接收到的上游响应缓冲区，通常，in链表是在input_filter方法中设置的，可参考ngx_event_pipe_copy_input_filter
    //方法，它会将接收到的缓冲区设置到in链表中
    ngx_chain_t       *in;
    //指向刚刚接收到的一个缓冲区
    ngx_chain_t      **last_in;

    //保存着将要发送给客户端的缓冲区链表，在写入临时文件成功时，会把in链表中写入文件的缓冲区添加到out链表中
    ngx_chain_t       *out;
    //等待释放的缓冲区
    ngx_chain_t       *free;
    //表示上次调用ngx_http_output_filter方法发送响应时没有发送完的缓冲区链表，这个链表中的缓冲区中已经保存到请求的
    //out链表中，busy仅用来记录还有多大的响应正等待发送
    ngx_chain_t       *busy;

    /*
     * the input filter i.e. that moves HTTP/1.1 chunks
     * from the raw bufs to an incoming chain
     */

    //处理接收到的来自上游服务器的缓冲区，一般使用upstream机制默认提供的ngx_event_pipe_copy_input_filter方法
    //作为input_filter
    ngx_event_pipe_input_filter_pt    input_filter;
    //用于input_filter方法的成员，一般将它设置为ngx_http_request_t结构体的地址
    void                             *input_ctx;

    //表示向下游发送响应的方法，默认使用ngx_http_output_filter方法作为output_filter
    ngx_event_pipe_output_filter_pt   output_filter;
    //指向ngx_http_request_t结构体
    void                             *output_ctx;

    //标志位，read为1表示当前已经读取到上游的响应
    unsigned           read:1;
    //标志位，为 1时表示启动文件缓存
    unsigned           cacheable:1;
    //标志位，为 1时表示接收上游响应时一次只能接收一个ngx_buf_t缓冲区
    unsigned           single_buf:1;
    //标志位，为1时一旦不再接收上游响应包体，将尽可能地立刻释放缓冲区。所谓尽可能是指，一旦这个缓冲区没有被引用，如果没有
    //用于写入临时文件或者用于向下游客户端释放，就把缓冲区指向的内存释放给pool内存池
    unsigned           free_bufs:1;
    //提供给HTTP模块在input_filter方法中使用的标志位，表示nginx与上游间的交互已结束，如果HTTP模块在解析包体时，认为
    //从业务上需要结束与上游间的连接，那么可以把upstream_done标志位置为1
    unsigned           upstream_done:1;
    //nginx与上游服务器间的连接出现错误时，upstream_error标志位为1，一般当接收上游响应超时，或者调用recv接收出现错误时
    //就会把该标志位设置为1
    unsigned           upstream_error:1;
    //表示与上游的连接状态，当nginx与上游服务器的连接已经关闭时，upstream_eof标志位为1
    unsigned           upstream_eof:1;
    //表示暂时阻塞住读取上游响应的流程，期待通过向下游发送响应来清理出空闲的缓冲区，再用空出的缓冲区接收响应，也就是说，upstream_blocked标志
    //为1时会在ngx_event_pipe方法的循环中先调用ngx_event_pipe_write_to_downstream方法发送响应，然后再次调用
    //ngx_event_pipe_read_upstream方法读取上游响应
    unsigned           upstream_blocked:1;
    //downstream_done标志位为1时表示与下游间的交互已经结束，目前无意义
    unsigned           downstream_done:1;
    //nginx与下游客户端的连接出现错误时，downstream_error标志位为1，在代码中一般是向下游发送响应超时，或者使用ngx_http_output_filter方法发送
    //响应却返回NGX_ERROR时，把downstream_error标志位设为1
    unsigned           downstream_error:1;
    //cyclic_temp_file标志位为1时会试图复用临时文件中曾经使用过的空间，不建议将cyclic_temp_file设为1，它是由ngx_http_upstream_conf_t配置结构体中的同名
    //成员赋值的
    unsigned           cyclic_temp_file:1;

    //表示已经分配的缓冲区数目，allocated受到bufs.num成员的限制
    ngx_int_t          allocated;
    //bufs记录了接收上游响应的内存缓冲区大小，其中bufs.size表示每个内存缓冲区的大小，而bufs.num表示最多可以有num个接收缓冲区
    ngx_bufs_t         bufs;
    //用于设置，比较缓冲区链表中ngx_buf_t结构体的tag标志位
    ngx_buf_tag_t      tag;


    ssize_t            busy_size;

    //已经接收到的上游响应包体长度
    off_t              read_length;
    off_t              length;

    //与ngx_http_upstream_conf_t配置结构体中的max_temp_file_size含义相同，同时它们的值也是相等的，表示临时文件的最大长度
    off_t              max_temp_file_size;
    //与ngx_http_upstream_conf_t配置体中的temp_file_write_size含义相同，同时它们的值也是相等的，表示一次写入文件时的最大长度
    ssize_t            temp_file_write_size;

    //读取上游响应的超时时间
    ngx_msec_t         read_timeout;
    //向下游发送响应的超时时间
    ngx_msec_t         send_timeout;
    //向下游发送响应时，TCP连接中设置的send_lowat"水位"
    ssize_t            send_lowat;

    //用于分配内存缓冲区的连接对象
    ngx_pool_t        *pool;
    //用于记录日志的ngx_log_t对象
    ngx_log_t         *log;

    //表示在接收上游服务器响应头部阶段，已经读取到的响应包体
    ngx_chain_t       *preread_bufs;
    //表示在接收上游服务器响应头部阶段，已经读取到的响应包体长度
    size_t             preread_size;
    //仅用于缓存文件的场景
    ngx_buf_t         *buf_to_file;

    //存放上游响应的临时文件，最大长度由max_temp_file_size成员限制
    ngx_temp_file_t   *temp_file;

    //已经使用的ngx_buf_t缓冲区数目
    /* STUB */ int     num;
};


ngx_int_t ngx_event_pipe(ngx_event_pipe_t *p, ngx_int_t do_write);
ngx_int_t ngx_event_pipe_copy_input_filter(ngx_event_pipe_t *p, ngx_buf_t *buf);
ngx_int_t ngx_event_pipe_add_free_buf(ngx_event_pipe_t *p, ngx_buf_t *b);


#endif /* _NGX_EVENT_PIPE_H_INCLUDED_ */
