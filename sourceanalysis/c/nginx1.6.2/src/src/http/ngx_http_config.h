
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#ifndef _NGX_HTTP_CONFIG_H_INCLUDED_
#define _NGX_HTTP_CONFIG_H_INCLUDED_


#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_http.h>

//配置模型，当nginx检查到http{}这个关键配置项的时候，http配置模型就启动了
typedef struct {
	//指针数组，数组中的每一个元素指向所有http模块create_main_conf方法产生结构体
	//指向一个指针数组，数组中的每一个成员都是由所有HTTP模块的create_main_conf
	//方法创建的存放全局配置项的结构体，它们存放着解析直属http{}块内的main级别的配置项参数
    void        **main_conf;
    //指针数组，数组中的每一个元素指向所有http模块create_srv_conf方法产生结构体
    /**
     * 指向一个指针数组，数组中的每个成员都是由所有的HTTP模块的create_srv_conf方法创建的与server相关的结构体
     * 它们或存放main级别配置项，或存放srv级别配置项，这与当前的ngx_http_conf_ctx_t是在解析http{}或者server{}
     * 块时创建的有关
     */
    void        **srv_conf;
    //指针数组，数组中的每一个元素指向所有http模块create_loc_conf方法产生结构体
    /**
     * 指向一个指针数组，数组中的每个成员都是由所有HTTP模块的create_loc_conf方法创建的与location相关的结构体
     * 它们可能存放着main,srv,loc级别的配置项，这与当前的ngx_http_conf_ctx_t是在解析http{},server{}或者locaton{}块时创建的有关
     *
     *
     */
    void        **loc_conf;
} ngx_http_conf_ctx_t;

//对于ngx_module_t中的ctx指针必须指向ngx_http_module_t接口
//http框架在读取，重载配置文件时定义了由ngx_http_module_t接口描述8个阶段，会在每一个阶段中调用ngx_http_module_t中相对于的方法
//如果ngx_http_module_t的某回调方法设为NULL空指针，那么HTTP框架是不会调用它的
//create_main_conf,create_srv_conf,create_loc_conf回调方法把我们分配的用于保存配置项的结构体传递到http框架
//当在nginx.conf中出现http{}时，http框架会接管配置文件中的http{}块的配置项的解析
//当遇到http{}配置块时，http框架会调用所有http模块可能实现的create_main_conf,create_srv_conf,create_loc_conf方法
//生成的存储main级别配置参数 的结构体;当遇到location{}时，会调用create_loc_conf回调方法生成loc级别的配置参数结构体;
//当遇到server{}时，会调用create_srv_conf回调方法生成srv级别的配置参数结构体

typedef struct {
	//在解析http{}内的配置项前回调
    ngx_int_t   (*preconfiguration)(ngx_conf_t *cf);
    //解析完http{}内的所有配置项后回调
    ngx_int_t   (*postconfiguration)(ngx_conf_t *cf);
    /**
     *当需要创建数据结构用于存储main级别的(值属于http{}块的配置项)的全局配置项时，可以通过create_main_conf回调方法创建
     *存储全局配置项的结构体
     *创建用于存储http全局配置项的结构体，该结构体中的成员将保存直属于http{}块的配置项参数，它会
     *在解析main配置项前调用
     */
    void       *(*create_main_conf)(ngx_conf_t *cf);

    //解析完main配置项后回调
    char       *(*init_main_conf)(ngx_conf_t *cf, void *conf);
    /**
     *当需要创建数据结构用于存储srv级别的(值属于server{}块的配置项)的全局配置项时，可以通过create_srv_conf回调方法创建
     *存储srv级别配置项的结构体      */
    //创建用于存储可同时出现在main，srv级别配置项的结构体，该结构体中的成员与server配置是相关联的
    void       *(*create_srv_conf)(ngx_conf_t *cf);
    //merge_srv_conf用于合并main级别和srv级别的同名配置项
    //create_srv_conf产生的结构体所要解析的配置项，可能同时出现在main，srv级别中，merge_srv_conf方法可以把出现在main
    //级别的配置项值合并到srv级别配置项中
    char       *(*merge_srv_conf)(ngx_conf_t *cf, void *prev, void *conf);
    /**
        *当需要创建数据结构用于存储loc级别的(值属于loc{}块的配置项)的全局配置项时，可以通过create_loc_conf回调方法创建
        *存储srv级别配置项的结构体      */
    //创建用于存储可同时出现在main,srv,loc级别配置项的结构体，该结构体中 的成员与location配置项是关联的
    void       *(*create_loc_conf)(ngx_conf_t *cf);
    //merge_loc_conf用于合并loc级别和srv级别的同名配置项
    //create_loc_conf产生的结构体所要解析的配置项，可能同时出现在main,srv,loc级别中，merge_loc_conf方法可以分别把
    //出现在main,srv级别的配置项值合并到loc级别的配置项中
    char       *(*merge_loc_conf)(ngx_conf_t *cf, void *prev, void *conf);
} ngx_http_module_t;


#define NGX_HTTP_MODULE           0x50545448   /* "HTTP" */

#define NGX_HTTP_MAIN_CONF        0x02000000
#define NGX_HTTP_SRV_CONF         0x04000000
#define NGX_HTTP_LOC_CONF         0x08000000
#define NGX_HTTP_UPS_CONF         0x10000000
#define NGX_HTTP_SIF_CONF         0x20000000
#define NGX_HTTP_LIF_CONF         0x40000000
#define NGX_HTTP_LMT_CONF         0x80000000


#define NGX_HTTP_MAIN_CONF_OFFSET  offsetof(ngx_http_conf_ctx_t, main_conf)
#define NGX_HTTP_SRV_CONF_OFFSET   offsetof(ngx_http_conf_ctx_t, srv_conf)
#define NGX_HTTP_LOC_CONF_OFFSET   offsetof(ngx_http_conf_ctx_t, loc_conf)


#define ngx_http_get_module_main_conf(r, module)                             \
    (r)->main_conf[module.ctx_index]
#define ngx_http_get_module_srv_conf(r, module)  (r)->srv_conf[module.ctx_index]
#define ngx_http_get_module_loc_conf(r, module)  (r)->loc_conf[module.ctx_index]


#define ngx_http_conf_get_module_main_conf(cf, module)                        \
    ((ngx_http_conf_ctx_t *) cf->ctx)->main_conf[module.ctx_index]
#define ngx_http_conf_get_module_srv_conf(cf, module)                         \
    ((ngx_http_conf_ctx_t *) cf->ctx)->srv_conf[module.ctx_index]
#define ngx_http_conf_get_module_loc_conf(cf, module)                         \
    ((ngx_http_conf_ctx_t *) cf->ctx)->loc_conf[module.ctx_index]

#define ngx_http_cycle_get_module_main_conf(cycle, module)                    \
    (cycle->conf_ctx[ngx_http_module.index] ?                                 \
        ((ngx_http_conf_ctx_t *) cycle->conf_ctx[ngx_http_module.index])      \
            ->main_conf[module.ctx_index]:                                    \
        NULL)


#endif /* _NGX_HTTP_CONFIG_H_INCLUDED_ */
