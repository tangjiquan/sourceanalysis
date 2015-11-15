
/* Frame object interface */

#ifndef Py_FRAMEOBJECT_H
#define Py_FRAMEOBJECT_H
#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    int b_type;			/* what kind of block this is */
    int b_handler;		/* where to jump to find handler */
    int b_level;		/* value stack level to pop to */
} PyTryBlock;

typedef struct _frame {
    PyObject_VAR_HEAD
    struct _frame *f_back;	/* previous frame, or NULL *///执行环境链上的前一个frame
    PyCodeObject *f_code;	/* code segment *///f_code中存放的是一个待执行的PyCodeObject对象
    /**
     * f_builtins，f_globals，f_locals是3个独立的名字空间，名字空间实际上是维护着变量名和变量值之间的关系的PyDictObject对象，所以在3个PyDictObject中
     * 分别维护了builtin的name，global的name，以及local的name与对应值之间的映射关系。
     */
    PyObject *f_builtins;	/* builtin symbol table (PyDictObject) *///builtin名字空间(Python层面)
    PyObject *f_globals;	/* global symbol table (PyDictObject) *///global名字空间(A.py层面)
    PyObject *f_locals;		/* local symbol table (any mapping) *///local名字空间(function函数层面)
    PyObject **f_valuestack;	/* points after the last local *///运行时栈的栈第位置
    /* Next free slot in f_valuestack.  Frame creation sets to f_valuestack.
       Frame evaluation usually NULLs it, but a frame that yields sets it
       to the current stack top. */
    PyObject **f_stacktop;//运行时栈的栈顶位置
    PyObject *f_trace;		/* Trace function *///

    /* If an exception is raised in this frame, the next three are used to
     * record the exception info (if any) originally in the thread state.  See
     * comments before set_exc_info() -- it's not obvious.
     * Invariant:  if _type is NULL, then so are _value and _traceback.
     * Desired invariant:  all three are NULL, or all three are non-NULL.  That
     * one isn't currently true, but "should be".
     */
    PyObject *f_exc_type, *f_exc_value, *f_exc_traceback;

    PyThreadState *f_tstate;
    int f_lasti;		/* Last instruction if called *///上一条字节码指令在f_code中的偏移位置
    /* As of 2.3 f_lineno is only valid when tracing is active (i.e. when
       f_trace is set) -- at other times use PyCode_Addr2Line instead. */
    int f_lineno;		/* Current line number *///当前字节码对应的源代码行
    int f_iblock;		/* index in f_blockstack */
    PyTryBlock f_blockstack[CO_MAXBLOCKS]; /* for try and loop blocks */
    PyObject *f_localsplus[1];	/* locals+stack, dynamically sized *///动态内存，维护(局部变量+cell对象集合+free对象集合+运行时栈)所需要的空间
} PyFrameObject;


/* Standard object interface */

PyAPI_DATA(PyTypeObject) PyFrame_Type;

#define PyFrame_Check(op) ((op)->ob_type == &PyFrame_Type)
#define PyFrame_IsRestricted(f) \
	((f)->f_builtins != (f)->f_tstate->interp->builtins)

PyAPI_FUNC(PyFrameObject *) PyFrame_New(PyThreadState *, PyCodeObject *,
                                       PyObject *, PyObject *);


/* The rest of the interface is specific for frame objects */

/* Block management functions */

PyAPI_FUNC(void) PyFrame_BlockSetup(PyFrameObject *, int, int, int);
PyAPI_FUNC(PyTryBlock *) PyFrame_BlockPop(PyFrameObject *);

/* Extend the value stack */

PyAPI_FUNC(PyObject **) PyFrame_ExtendStack(PyFrameObject *, int, int);

/* Conversions between "fast locals" and locals in dictionary */

PyAPI_FUNC(void) PyFrame_LocalsToFast(PyFrameObject *, int);
PyAPI_FUNC(void) PyFrame_FastToLocals(PyFrameObject *);

#ifdef __cplusplus
}
#endif
#endif /* !Py_FRAMEOBJECT_H */
