# 编译原理实践

包含四个实验
- 词法分析, Java_LexAnalysis
- LL文法分析, Java_LLParserAnalysis
- LR文法分析, Java_LRParserAnalysis
- 语义分析, Java_TranslationSchemaAnalysis

实验采用oj形式评判（就连java类名都不规范），所以每个实验所有代码全都塞到一个文件里了， 
导致可读性极大幅度降低、重复代码行数极多，加上注释很少，请酌情阅读。

测试点/测试代码在test下，目前测试用例换行符, trailing whitespace这种还未统一，还请人工对比。

错误处理很不完善，仅针对某种情况生效，请不要参考。