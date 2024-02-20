package json.sql.shell.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import json.sql.JsonSqlContext;
import json.sql.grammar.ParserErrorListener;
import json.sql.shell.ShellContext;
import json.sql.shell.annotation.CommandClass;
import json.sql.shell.annotation.CommandMethod;
import json.sql.shell.annotation.CommandMethodIgnore;
import json.sql.shell.annotation.CommandParam;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CommandClass
public class SqlCommand {

    @CommandMethodIgnore
    public static boolean isSql(String sql){
        return ShellContext.cur().getJsonSqlContext().isSql(sql);
    }

    @CommandMethodIgnore
    public static List<String> sqlHasError(String sql){
        return ShellContext.cur().getJsonSqlContext().getSqlError(sql);
    }

    @CommandMethod(name = {"dataSetSql"},desc = "执行数据集的sql语句")
    public Boolean dataSetSql(@CommandParam(desc = "需要执行的sql,多个以;分割") String sql,@CommandParam(desc = "存放结果的目录") String tempDir,
                              @CommandParam(desc = "是否将临时目录下的结果文件替换原数据") Boolean removeCurData){
        if(ObjectUtil.isEmpty(tempDir)){
            tempDir = ShellContext.cur().getDefaultTempDataPath();
            File file = new File(tempDir);
            Console.log("default tmpDir is : {}",file.getAbsolutePath());
        }
        if(!FileUtil.exist(tempDir)){
            FileUtil.mkdir(tempDir);
        }
        JsonSqlContext jsonSqlContext = ShellContext.cur().getJsonSqlContext();
        Set<Map.Entry<String, String>> entries = DDLCommand.dataSet.entrySet();
        String finalTempDir = tempDir;
        for (Map.Entry<String, String> entry : entries) {
            String path = entry.getKey();
            String tableName = entry.getValue();
            String filePath = finalTempDir +FileUtil.FILE_SEPARATOR+FileUtil.getName(path);
            FileUtil.del(filePath);
            FileUtil.readUtf8Lines(new File(path), (LineHandler) s -> {
                if (!jsonSqlContext.isJsonData(s)) {
                    return;
                }
                jsonSqlContext.dropTable(tableName);
                jsonSqlContext.registerTable(tableName, s);
                String tempSql = sql.replaceAll("\\$\\$curTable", tableName);
                jsonSqlContext.sql(tempSql);
                String table = jsonSqlContext.getTable(tableName);
                if(ObjectUtil.isNotEmpty(table)){
                    FileUtil.appendUtf8String(table+"\n",filePath);
                }
            });
            if(ObjectUtil.isNotEmpty(removeCurData) && removeCurData){
                if(FileUtil.exist(filePath)){
                    FileUtil.move(new File(filePath),new File(path),true);
                }else{
                    FileUtil.writeUtf8String("", path);
                }
            }
        }
        return true;
    }

    @CommandMethod(name = {"sql"},desc = "执行sql语句")
    public String sql(@CommandParam(desc = "需要执行的sql,多个以;分割") String sql){
        List<String> errors = sqlHasError(sql);
        if(!errors.isEmpty()){
            throw new RuntimeException(String.join("\n",errors));
        }
        return ShellContext.cur().getJsonSqlContext().sql(sql);
    }

}
