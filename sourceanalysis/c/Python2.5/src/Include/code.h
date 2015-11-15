/* Definitions for bytecode */

#ifndef Py_CODE_H
#define Py_CODE_H
#ifdef __cplusplus
extern "C" {
#endif

/* Bytecode object */
typedef struct {
    PyObject_HEAD
    int co_argcount;		/* #arguments, except *args *///Code Block的位置参数的个数，比如说一个函数的位置参数个数
    int co_nlocals;		/* #local variables *///Code Block中局部变量的个数，包括其位置参数的个数
    int co_stacksize;		/* #entries needed for evaluation stack *///执行该段Code Block需要的栈空间
    int co_flags;		/* CO_..., see below *///N/A
    PyObject *co_code;		/* instruction opcodes *///存放编译所生成的字节码指令序列，以PyStringObject的形式存在
    PyObject *co_consts;	/* list (constants used) *///PyTupleObject对象，保存Code Block中的所有常量
    PyObject *co_names;		/* list of strings (names used) *///PyTupleObject对象，保存Code Block中的所有符号
    PyObject *co_varnames;	/* tuple of strings (local variable names) *///Code Block中局部变量名集合
    PyObject *co_freevars;	/* tuple of strings (free variable names) *///Python实现闭包需要用到的东西
    PyObject *co_cellvars;      /* tuple of strings (cell variable names) *///Code Block中内部嵌套函数所引用的局部变量名集合
    /* The rest doesn't count for hash/cmp *///
    PyObject *co_filename;	/* string (where it was loaded from) *///Code Block的名字，通常是.py文件的完整路径
    PyObject *co_name;		/* string (name, for reference) *///Code Block的名字，通常是函数名或者类名
    int co_firstlineno;		/* first source line number *///Code Block在对应的.py文件中的起始行
    PyObject *co_lnotab;	/* string (encoding addr<->lineno mapping) *///字节码指令与.py文件中source code行号的对应关系，以PyStringObject的形式存在
    void *co_zombieframe;     /* for optimization only (see frameobject.c) */
} PyCodeObject;

/* Masks for co_flags above */
#define CO_OPTIMIZED	0x0001
#define CO_NEWLOCALS	0x0002
#define CO_VARARGS	0x0004
#define CO_VARKEYWORDS	0x0008
#define CO_NESTED       0x0010
#define CO_GENERATOR    0x0020
/* The CO_NOFREE flag is set if there are no free or cell variables.
   This information is redundant, but it allows a single flag test
   to determine whether there is any extra work to be done when the
   call frame it setup.
*/
#define CO_NOFREE       0x0040

#if 0
/* This is no longer used.  Stopped defining in 2.5, do not re-use. */
#define CO_GENERATOR_ALLOWED    0x1000
#endif
#define CO_FUTURE_DIVISION    	0x2000
#define CO_FUTURE_ABSOLUTE_IMPORT 0x4000 /* do absolute imports by default */
#define CO_FUTURE_WITH_STATEMENT  0x8000

/* This should be defined if a future statement modifies the syntax.
   For example, when a keyword is added.
*/
#define PY_PARSER_REQUIRES_FUTURE_KEYWORD

#define CO_MAXBLOCKS 20 /* Max static block nesting within a function */

PyAPI_DATA(PyTypeObject) PyCode_Type;

#define PyCode_Check(op) ((op)->ob_type == &PyCode_Type)
#define PyCode_GetNumFree(op) (PyTuple_GET_SIZE((op)->co_freevars))

/* Public interface */
PyAPI_FUNC(PyCodeObject *) PyCode_New(
	int, int, int, int, PyObject *, PyObject *, PyObject *, PyObject *,
	PyObject *, PyObject *, PyObject *, PyObject *, int, PyObject *); 
        /* same as struct above */
PyAPI_FUNC(int) PyCode_Addr2Line(PyCodeObject *, int);

/* for internal use only */
#define _PyCode_GETCODEPTR(co, pp) \
	((*(co)->co_code->ob_type->tp_as_buffer->bf_getreadbuffer) \
	 ((co)->co_code, 0, (void **)(pp)))

typedef struct _addr_pair {
        int ap_lower;
        int ap_upper;
} PyAddrPair;

/* Check whether lasti (an instruction offset) falls outside bounds
   and whether it is a line number that should be traced.  Returns
   a line number if it should be traced or -1 if the line should not.

   If lasti is not within bounds, updates bounds.
*/

PyAPI_FUNC(int) PyCode_CheckLineNumber(PyCodeObject* co,
                                       int lasti, PyAddrPair *bounds);

#ifdef __cplusplus
}
#endif
#endif /* !Py_CODE_H */
