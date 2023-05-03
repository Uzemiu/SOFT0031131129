## 任务描述
本关任务：用 C/C++ 编写一个 C 语言的语法分析器程序。

## 相关知识
为了完成本关任务，你需要掌握：1.DFA NFA，2.C/C++ 编程语言基础。3. C 语言的基本结构知识

## 自动机
在编译原理课堂上已经教授了大家相关知识。在完成本实训前，一定要先设计相关自动机，再开始相关功能的实现。切勿，想到哪里，就编程到哪里，以至于代码一团糟，可维护性与可读性都很差。

## C/C++
本实训涉及函数、结构体，标准流输入输出，字符串等操作

## C语言基本结构
C 语言子集。
第一类：标识符
第二类：常数
第三类：保留字(32)

```
auto       break    case     char        const      continue
default    do       double   else        enum       extern
float      for      goto     if          int        long
register   return   short    signed      sizeof     static
struct     switch   typedef  union       unsigned   void
volatile    while
```
第四类：界符  /\*、//、 ()、 { }、[ ]、" " 、 ' 等
第五类：运算符 <、<=、>、>=、=、+、-、*、/、^等

**所有语言元素集合在 c_keys.txt**文件中。
注意，C_key.txt中缺少“//注释”的情况，请也映射到编号79！