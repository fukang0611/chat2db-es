# chat2db-es
chat to es database by ai 

使用 spring ai 开发一款用于智能问数（针对 es 数据库）的程序，实现将用户的自然语言转换为 dsl 并通过 elasticsearch-java 9.x 客户端执行.

具体步骤：
1. 用户针对 es 数据库进行自然语言提问；
2. 使用 AI 大模型将用户问题转换为具体的 DSL 查询语句；
3. 调用 elasticsearch-java 9.x 客户端执行该 DSL 并获取响应结果；
4. 根据查询类型分为原始文档数据查询和聚合统计类查询，原始文档查询则使用对应的实体类进行接收，聚合统计类则将统计结果以 json 格式返回。
