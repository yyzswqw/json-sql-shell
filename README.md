# json-sql-shell
这是json-sql的shell交互界面终端。基于json-sql实现的。

json-sql地址：https://github.com/yyzswqw/json-sql

# 提供的功能

- 注册表数据
- 注册dataSet数据集
- 修改表配置
- 注册表数据执行SQL

使用`java -classpath json-sql-shell.jar json.sql.shell.App `启动程序，使用`help`命令查看可使用的命令，使用`sqlUdf`命令查看可使用的所有的`UDF`函数。按`Tab`可提示可使用的命令。

# `help`命令使用

```shell
json-sql-shell:> help
sqlUdf
	desc: 展示所有注册的udf函数
	Returns: void
	Args:
		udfName
			String         	sql中udf函数名称，为空时查询全部

q
	desc: 退出当前终端
	Returns: void
	Args:
		None

exit
	desc: 退出当前终端
	Returns: void
	Args:
		None

help
	desc: 展示可使用的命令
	Returns: void
	Args:
		commandName
			String         	命令名称，为空时查询全部

createTable
	desc: 新增表
	Returns: boolean
	Args:
		dataPath
			String         	数据文件路径，包括文件名
		tableName
			String         	表名

addDataSet
	desc: 添加json数据集文件，每个文件中一行数据为一条json
	Returns: String
	Args:
		path
			String         	保存路径,为空时将保存到原文件中

h
	desc: 展示可使用的命令
	Returns: void
	Args:
		commandName
			String         	命令名称，为空时查询全部

save
	desc: 保存表数据
	Returns: String
	Args:
		tableName
			String         	表名
		path
			String         	保存路径,为空时将保存到原文件中

quit
	desc: 退出当前终端
	Returns: void
	Args:
		None

dataSetSql
	desc: 执行数据集的sql语句
	Returns: Boolean
	Args:
		sql
			String         	需要执行的sql,多个以;分割
		tempDir
			String         	存放结果的目录
		removeCurData
			Boolean        	是否将临时目录下的结果文件替换原数据

sql
	desc: 执行sql语句
	Returns: String
	Args:
		sql
			String         	需要执行的sql,多个以;分割

setConfig
	desc: 设置表的属性
	Returns: void
	Args:
		tableName
			String         	表名
		key
			String         	key
		value
			Object         	value


```

# `sqlUdf`命令使用

```shell
json-sql-shell:> sqlUdf subString
subString
	desc: 截取字符串一部分
	Source By Class : unknown
	Returns: String
	Args:
		data
			String         	待截取数据
		start
			Integer        	截取的开始下标(包含)，从0开始，为空则从0开始，若为负数则从最后往前推
		end
			Integer        	截取的结束下标(包含)，为空则截取到最后一个，若为负数则从最后往前推
```



# 注意事项

注册表数据使用`createTable`命令，数据文件中只能有一条JSON数据，可以换行，无须在一行中，使用`drop table tableName`删除表。

注册数据集使用`addDataSet`命令，目录下可以有子目录，数据文件必须是`.txt`或者`.json`的后缀扩展名。且文件中可以有多个JSON数据，但是必须是一行一个JSON。

执行表数据中的`SQL`时，直接写`SQL`执行即可，等同与`sql`命令。

执行数据集中数据中的`SQL`时，需要使用`dataSetSql`命令执行。
