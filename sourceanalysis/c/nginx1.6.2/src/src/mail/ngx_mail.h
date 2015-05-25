
/*
 * Copyright (C) Igor Sysoev
 * Copyright (C) Nginx, Inc.
 */


#ifndef _NGX_MAIL_H_INCLUDED_
#define _NGX_MAIL_H_INCLUDED_


#include <ngx_config.h>
#include <ngx_core.h>
#include <ngx_event.h>
#include <ngx_event_connect.h>

#if (NGX_MAIL_SSL)
#include <ngx_mail_ssl_module.h>
#endif



typedef struct {
    void                  **main_conf;
    void                  **srv_conf;
} ngx_mail_conf_ctx_t;


typedef struct {
    u_char                  sockaddr[NGX_SOCKADDRLEN];
    socklen_t               socklen;

    /* server ctx */
    ngx_mail_conf_ctx_t    *ctx;

    unsigned                bind:1;
    unsigned                wildcard:1;
#if (NGX_MAIL_SSL)
    unsigned                ssl:1;
#endif
#if (NGX_HAVE_INET6 && defined IPV6_V6ONLY)
    unsigned                ipv6only:1;
#endif
    unsigned                so_keepalive:2;
#if (NGX_HAVE_KEEPALIVE_TUNABLE)
    int                     tcp_keepidle;
    int                     tcp_keepintvl;
    int                     tcp_keepcnt;
#endif
} ngx_mail_listen_t;


typedef struct {
    ngx_mail_conf_ctx_t    *ctx;
    ngx_str_t               addr_text;
#if (NGX_MAIL_SSL)
    ngx_uint_t              ssl;    /* unsigned   ssl:1; */
#endif
} ngx_mail_addr_conf_t;

typedef struct {
    in_addr_t               addr;
    ngx_mail_addr_conf_t    conf;
} ngx_mail_in_addr_t;


#if (NGX_HAVE_INET6)

typedef struct {
    struct in6_addr         addr6;
    ngx_mail_addr_conf_t    conf;
} ngx_mail_in6_addr_t;

#endif


typedef struct {
    /* ngx_mail_in_addr_t or ngx_mail_in6_addr_t */
    void                   *addrs;
    ngx_uint_t              naddrs;
} ngx_mail_port_t;


typedef struct {
    int                     family;
    in_port_t               port;
    ngx_array_t             addrs;       /* array of ngx_mail_conf_addr_t */
} ngx_mail_conf_port_t;


typedef struct {
    struct sockaddr        *sockaddr;
    socklen_t               socklen;

    ngx_mail_conf_ctx_t    *ctx;

    unsigned                bind:1;
    unsigned                wildcard:1;
#if (NGX_MAIL_SSL)
    unsigned                ssl:1;
#endif
#if (NGX_HAVE_INET6 && defined IPV6_V6ONLY)
    unsigned                ipv6only:1;
#endif
    unsigned                so_keepalive:2;
#if (NGX_HAVE_KEEPALIVE_TUNABLE)
    int                     tcp_keepidle;
    int                     tcp_keepintvl;
    int                     tcp_keepcnt;
#endif
} ngx_mail_conf_addr_t;


typedef struct {
    ngx_array_t             servers;     /* ngx_mail_core_srv_conf_t */
    ngx_array_t             listen;      /* ngx_mail_listen_t */
} ngx_mail_core_main_conf_t;


#define NGX_MAIL_POP3_PROTOCOL  0
#define NGX_MAIL_IMAP_PROTOCOL  1
#define NGX_MAIL_SMTP_PROTOCOL  2


typedef struct ngx_mail_protocol_s  ngx_mail_protocol_t;


typedef struct {
    ngx_mail_protocol_t    *protocol;

    ngx_msec_t              timeout;
    ngx_msec_t              resolver_timeout;

    ngx_flag_t              so_keepalive;

    ngx_str_t               server_name;

    u_char                 *file_name;
    ngx_int_t               line;

    ngx_resolver_t         *resolver;

    /* server ctx */
    ngx_mail_conf_ctx_t    *ctx;
} ngx_mail_core_srv_conf_t;


typedef enum {
    ngx_pop3_start = 0,
    ngx_pop3_user,
    ngx_pop3_passwd,
    ngx_pop3_auth_login_username,
    ngx_pop3_auth_login_password,
    ngx_pop3_auth_plain,
    ngx_pop3_auth_cram_md5
} ngx_pop3_state_e;


typedef enum {
    ngx_imap_start = 0,
    ngx_imap_auth_login_username,
    ngx_imap_auth_login_password,
    ngx_imap_auth_plain,
    ngx_imap_auth_cram_md5,
    ngx_imap_login,
    ngx_imap_user,
    ngx_imap_passwd
} ngx_imap_state_e;


typedef enum {
    ngx_smtp_start = 0,
    ngx_smtp_auth_login_username,
    ngx_smtp_auth_login_password,
    ngx_smtp_auth_plain,
    ngx_smtp_auth_cram_md5,
    ngx_smtp_helo,
    ngx_smtp_helo_xclient,
    ngx_smtp_helo_from,
    ngx_smtp_xclient,
    ngx_smtp_xclient_from,
    ngx_smtp_xclient_helo,
    ngx_smtp_from,
    ngx_smtp_to
} ngx_smtp_state_e;


typedef struct {
	//与上游邮件服务器间的连接
    ngx_peer_connection_t   upstream;
    //用于缓存上下游间TCP消息的内存缓冲区，内存大小由nginx.conf文件中的proxy_buffer配置项决定
    ngx_buf_t              *buffer;
} ngx_mail_proxy_ctx_t;


typedef struct {
	//目前没有使用
    uint32_t                signature;         /* "MAIL" */

    //下游客户端与nginx之间的连接
    ngx_connection_t       *connection;

    //out中可以存放需要向下游客户端发送的内容
    ngx_str_t               out;
    //这个缓冲区用于接收来自客户端的请求。这个缓冲区中所使用的内存大小与请求是有关系的，对于POP3请求固定为128个字节，对于
    //SMTP请求，由nginx.conf配置文件中的smtp_client_buffer配置项决定，对于IMAP请求，则由imap_client_buffer配置项决定
    ngx_buf_t              *buffer;

    //**ctx将指向一个指针数组，它的含义与HTTP请求的ngx_http_request_t结构体中的ctx一致，保存着这个请求中各个邮件
    //模块的上下文结构体指针
    void                  **ctx;
    //main级别配置结构体组成的指针数组
    void                  **main_conf;
    //srv级别配置结构体组成的指针数组
    void                  **srv_conf;

    //解析主机域名
    ngx_resolver_ctx_t     *resolver_ctx;

    //请求经过认证后，nginx就开始代理客户端与邮件服务器间的通信，这时会生成proxy上下文用于此目的
    ngx_mail_proxy_ctx_t   *proxy;

    //表示与邮件服务器交互时，当前处于哪几种状态。对于POP3请求来说，会隶属于ngx_pop3_state_e定义的7种状态；对于
    //IMAP请求来说，会隶属于ngx_imap_state_e定义的8种状态：对于SMTP请求来说，会隶属于ngx_smtp_state_e定义的13种状态
    ngx_uint_t              mail_state;

    //邮件协议类型目前仅由以下3个
    //#define NGX_MAIL_POP3_PROTOCOL 0
    //#define NGX_MAIL_IMAP_PROTOCOL 1
    //#define NGX_MAIL_SMTP_PROTOCOL 2
    unsigned                protocol:3;
    //标志位。blocked为1时表示当前的读或写操作需要被阻塞
    unsigned                blocked:1;
    //标志位。quit为1时表示请求需要结束
    unsigned                quit:1;
    //以下3个标志位仅在解析具体的邮件协议时由邮件框架使用
    unsigned                quoted:1;
    unsigned                backslash:1;
    unsigned                no_sync_literal:1;
    //当使用SSL协议时，该标志位为1说明使用TLS传输层安全协议
    unsigned                starttls:1;
    //
    unsigned                esmtp:1;
    //表示与认证服务器交互时的记录认证方式。目前有6个预设值
    //define NGX_MAIL_AUTH_PLAIN 0
    //define NGX_MAIL_AUTH_LOGIN 1
    //define NGX_MAIL_AUTH_LOGIN_USERNAME 2
    //define NGX_MAIL_AUTH_APOP 3
    //define NGX_MAIL_AUTH_CRAM_MD5 4
    //define NGX_MAIL_AUTH_NONE 5
    unsigned                auth_method:3;
    //用于认证服务器的标志位，为1时表示得知认证服务器要求暂缓接收响应，这时nginx会继续等待认证服务器的后续响应
    unsigned                auth_wait:1;

    //用于验证的用户名，在于认证服务器交互后会被设为认证服务器返回的响应中的auth-user头部
    ngx_str_t               login;
    //相对于login用户名的密码，与认证服务器交互后会被设为认证服务器返回的响应中的auth-pass头部
    ngx_str_t               passwd;

    //作为auth-salt验证的信息
    ngx_str_t               salt;
    //以下3个成员仅用于IMAP通信
    ngx_str_t               tag;
    ngx_str_t               tagged_line;
    ngx_str_t               text;

    //当前连接上对应的nginx服务器地址
    ngx_str_t              *addr_text;
    //主机地址
    ngx_str_t               host;
    //以下3个成员仅用于SMTP的通信
    ngx_str_t               smtp_helo;
    ngx_str_t               smtp_from;
    ngx_str_t               smtp_to;

    //
    ngx_str_t               cmd;

    //在与邮件服务器交互时（即与认证服务器交互后，透传上下游TCP流之前），command表示解析自邮件服务器的消息类型
    ngx_uint_t              command;
    //args动态数组中会存放来自下游客户端的邮件协议中的参数
    ngx_array_t             args;

    //当前请求尝试访问认证服务器验证的次数
    ngx_uint_t              login_attempt;

    /* used to parse POP3/IMAP/SMTP command */

    //以下成员用于解析POP3/IMAP/SMTP等协议的命令行
    ngx_uint_t              state;
    u_char                 *cmd_start;
    u_char                 *arg_start;
    u_char                 *arg_end;
    ngx_uint_t              literal_len;
} ngx_mail_session_t;


typedef struct {
    ngx_str_t              *client;
    ngx_mail_session_t     *session;
} ngx_mail_log_ctx_t;


#define NGX_POP3_USER          1
#define NGX_POP3_PASS          2
#define NGX_POP3_CAPA          3
#define NGX_POP3_QUIT          4
#define NGX_POP3_NOOP          5
#define NGX_POP3_STLS          6
#define NGX_POP3_APOP          7
#define NGX_POP3_AUTH          8
#define NGX_POP3_STAT          9
#define NGX_POP3_LIST          10
#define NGX_POP3_RETR          11
#define NGX_POP3_DELE          12
#define NGX_POP3_RSET          13
#define NGX_POP3_TOP           14
#define NGX_POP3_UIDL          15


#define NGX_IMAP_LOGIN         1
#define NGX_IMAP_LOGOUT        2
#define NGX_IMAP_CAPABILITY    3
#define NGX_IMAP_NOOP          4
#define NGX_IMAP_STARTTLS      5

#define NGX_IMAP_NEXT          6

#define NGX_IMAP_AUTHENTICATE  7


#define NGX_SMTP_HELO          1
#define NGX_SMTP_EHLO          2
#define NGX_SMTP_AUTH          3
#define NGX_SMTP_QUIT          4
#define NGX_SMTP_NOOP          5
#define NGX_SMTP_MAIL          6
#define NGX_SMTP_RSET          7
#define NGX_SMTP_RCPT          8
#define NGX_SMTP_DATA          9
#define NGX_SMTP_VRFY          10
#define NGX_SMTP_EXPN          11
#define NGX_SMTP_HELP          12
#define NGX_SMTP_STARTTLS      13


#define NGX_MAIL_AUTH_PLAIN             0
#define NGX_MAIL_AUTH_LOGIN             1
#define NGX_MAIL_AUTH_LOGIN_USERNAME    2
#define NGX_MAIL_AUTH_APOP              3
#define NGX_MAIL_AUTH_CRAM_MD5          4
#define NGX_MAIL_AUTH_NONE              5


#define NGX_MAIL_AUTH_PLAIN_ENABLED     0x0002
#define NGX_MAIL_AUTH_LOGIN_ENABLED     0x0004
#define NGX_MAIL_AUTH_APOP_ENABLED      0x0008
#define NGX_MAIL_AUTH_CRAM_MD5_ENABLED  0x0010
#define NGX_MAIL_AUTH_NONE_ENABLED      0x0020


#define NGX_MAIL_PARSE_INVALID_COMMAND  20

//4个POP3，SMTP,IMAP等应用级别的邮件模块所要实现的接口方法
typedef void (*ngx_mail_init_session_pt)(ngx_mail_session_t *s,
    ngx_connection_t *c);
typedef void (*ngx_mail_init_protocol_pt)(ngx_event_t *rev);
typedef void (*ngx_mail_auth_state_pt)(ngx_event_t *rev);
typedef ngx_int_t (*ngx_mail_parse_command_pt)(ngx_mail_session_t *s);


struct ngx_mail_protocol_s {
	//邮件模块名称
    ngx_str_t                   name;
    //当前邮件模块中所要监听的最常用的4个端口
    in_port_t                   port[4];
    //邮件模块类型，目前type仅可以取值为:NGX_MAIL_POP3_PROTOCOL,NGX_MAIL_IMAP_PROTOCOL,NGX_MAIL_SMTP_PROTOCOL
    ngx_uint_t                  type;

    //与客户端建立起TCP连接后的初始化方法
    ngx_mail_init_session_pt    init_session;
    //接收，解析客户端请求的方法
    ngx_mail_init_protocol_pt   init_protocol;
    //解析客户端邮件协议的接口方法，由POP3，SMTP,IMAP	等邮件模块实现
    ngx_mail_parse_command_pt   parse_command;
    //认证客户端请求的方法
    ngx_mail_auth_state_pt      auth_state;

    //当处理过程中出现没有预见到的错误时，将会返回internal_server_error指定的响应到客户端
    ngx_str_t                   internal_server_error;
};


typedef struct {
	//POP3,SMTP,IMAP邮件模块提取出的通用接口
    ngx_mail_protocol_t        *protocol;

    //创建用于存储main级别配置项的结构体，该结构体中的成员将保存直属于main{}块的配置项参数
    void                       *(*create_main_conf)(ngx_conf_t *cf);
    //解析完main级别配置项后被回调
    char                       *(*init_main_conf)(ngx_conf_t *cf, void *conf);

    //创建用于存储srv级别配置项的结构体，该结构体中的成员将保存直属于server{}块的配置项参数
    void                       *(*create_srv_conf)(ngx_conf_t *cf);
    //srv级别可能在于main级别同名的配置项，该回调方法会给具体的邮件模块提供一个手段，以便从prev和
    //conf参数中获取到已经解析完毕的main和srv配置项结构体，自由地重新修改它们的值
    char                       *(*merge_srv_conf)(ngx_conf_t *cf, void *prev,
                                      void *conf);
} ngx_mail_module_t;


#define NGX_MAIL_MODULE         0x4C49414D     /* "MAIL" */

#define NGX_MAIL_MAIN_CONF      0x02000000
#define NGX_MAIL_SRV_CONF       0x04000000


#define NGX_MAIL_MAIN_CONF_OFFSET  offsetof(ngx_mail_conf_ctx_t, main_conf)
#define NGX_MAIL_SRV_CONF_OFFSET   offsetof(ngx_mail_conf_ctx_t, srv_conf)


#define ngx_mail_get_module_ctx(s, module)     (s)->ctx[module.ctx_index]
#define ngx_mail_set_ctx(s, c, module)         s->ctx[module.ctx_index] = c;
#define ngx_mail_delete_ctx(s, module)         s->ctx[module.ctx_index] = NULL;


#define ngx_mail_get_module_main_conf(s, module)                             \
    (s)->main_conf[module.ctx_index]
#define ngx_mail_get_module_srv_conf(s, module)  (s)->srv_conf[module.ctx_index]

#define ngx_mail_conf_get_module_main_conf(cf, module)                       \
    ((ngx_mail_conf_ctx_t *) cf->ctx)->main_conf[module.ctx_index]
#define ngx_mail_conf_get_module_srv_conf(cf, module)                        \
    ((ngx_mail_conf_ctx_t *) cf->ctx)->srv_conf[module.ctx_index]


#if (NGX_MAIL_SSL)
void ngx_mail_starttls_handler(ngx_event_t *rev);
ngx_int_t ngx_mail_starttls_only(ngx_mail_session_t *s, ngx_connection_t *c);
#endif


void ngx_mail_init_connection(ngx_connection_t *c);

ngx_int_t ngx_mail_salt(ngx_mail_session_t *s, ngx_connection_t *c,
    ngx_mail_core_srv_conf_t *cscf);
ngx_int_t ngx_mail_auth_plain(ngx_mail_session_t *s, ngx_connection_t *c,
    ngx_uint_t n);
ngx_int_t ngx_mail_auth_login_username(ngx_mail_session_t *s,
    ngx_connection_t *c, ngx_uint_t n);
ngx_int_t ngx_mail_auth_login_password(ngx_mail_session_t *s,
    ngx_connection_t *c);
ngx_int_t ngx_mail_auth_cram_md5_salt(ngx_mail_session_t *s,
    ngx_connection_t *c, char *prefix, size_t len);
ngx_int_t ngx_mail_auth_cram_md5(ngx_mail_session_t *s, ngx_connection_t *c);
ngx_int_t ngx_mail_auth_parse(ngx_mail_session_t *s, ngx_connection_t *c);

void ngx_mail_send(ngx_event_t *wev);
ngx_int_t ngx_mail_read_command(ngx_mail_session_t *s, ngx_connection_t *c);
void ngx_mail_auth(ngx_mail_session_t *s, ngx_connection_t *c);
void ngx_mail_close_connection(ngx_connection_t *c);
void ngx_mail_session_internal_server_error(ngx_mail_session_t *s);
u_char *ngx_mail_log_error(ngx_log_t *log, u_char *buf, size_t len);


char *ngx_mail_capabilities(ngx_conf_t *cf, ngx_command_t *cmd, void *conf);


/* STUB */
void ngx_mail_proxy_init(ngx_mail_session_t *s, ngx_addr_t *peer);
void ngx_mail_auth_http_init(ngx_mail_session_t *s);
/**/


extern ngx_uint_t    ngx_mail_max_module;
extern ngx_module_t  ngx_mail_core_module;


#endif /* _NGX_MAIL_H_INCLUDED_ */
