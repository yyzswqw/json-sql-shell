package json.sql.shell.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import json.sql.shell.ShellContext;
import json.sql.shell.annotation.CommandClass;
import json.sql.shell.annotation.CommandMethod;
import json.sql.shell.annotation.CommandMethodIgnore;
import json.sql.shell.annotation.CommandParam;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandClass
public class DDLCommand {

    public static final Map<String,String> tablePathMap = new HashMap<>();

    public static final Map<String,String> dataSet = new HashMap<>();

    public static final Map<String,String> dataSetTablePathMap = new HashMap<>();

    @CommandMethod(name = {"addDataSet"},desc = "添加json数据集文件，每个文件中一行数据为一条json")
    public String addDataSet(@CommandParam(desc = "保存路径,为空时将保存到原文件中")String path){
        if(!FileUtil.exist(path)){
            throw new RuntimeException("path 不存在");
        }
        int size = 0;
        File file = new File(path);
        if (FileUtil.isFile(file)) {
            String absolutePath = FileUtil.getAbsolutePath(file);
            String fullTableName = getFullTableName(absolutePath);
            dataSetTablePathMap.put(fullTableName, path);
            dataSet.put(path, fullTableName);
            size+=1;
        }else if(FileUtil.isDirectory(path)){
            List<File> files = FileUtil.loopFiles(path);
            for (File f : files) {
                String extName = FileUtil.extName(f);
                if(ObjectUtil.isEmpty(extName) || (!"txt".equalsIgnoreCase(extName) && !"json".equalsIgnoreCase(extName))){
                    continue;
                }
                String fullTableName = getFullTableName(f.getAbsolutePath());
                dataSetTablePathMap.put(fullTableName, f.getAbsolutePath());
                dataSet.put(f.getAbsolutePath(), fullTableName);
                size+=1;
            }
        }
        return "register dataSet size: " + size;
    }

    @CommandMethodIgnore
    public String getFullTableName(String path){
        String s = path.replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("/", "_").replaceAll("\\.", "_");
        if(s.startsWith("_")){
            return s.substring(1);
        }
        return s;
    }

    @CommandMethod(name = {"save"},desc = "保存表数据")
    public String save(@CommandParam(desc = "表名")String tableName,
                       @CommandParam(desc = "保存路径,为空时将保存到原文件中")String path){
        if(ObjectUtil.hasEmpty(tableName)){
            throw new RuntimeException("表名不能为空");
        }
        if (!tablePathMap.containsKey(tableName)) {
            throw new RuntimeException("表不存在");
        }
        if(ObjectUtil.isEmpty(path)){
            path = tablePathMap.get(tableName);
        }

        String table = ShellContext.cur().getJsonSqlContext().getTable(tableName);
        if (FileUtil.isFile(path)) {
            File file = new File(path);
            FileUtil.writeUtf8String(table,file);
            return file.getAbsolutePath();
        }else if(FileUtil.isDirectory(path)){
            File file = new File(path+FileUtil.FILE_SEPARATOR+tableName+".json");
            FileUtil.writeUtf8String(table,file);
            return file.getAbsolutePath();
        }else{
            throw new RuntimeException("路径不正确! path: "+path);
        }
    }

    @CommandMethod(name = {"setConfig"},desc = "设置表的属性")
    public void setConfig(@CommandParam(desc = "表名")String tableName,
                          @CommandParam(desc = "key")String key,
                          @CommandParam(desc = "value")Object value){
        if(ObjectUtil.hasEmpty(tableName,key,value)){
            throw new RuntimeException("参数不能为空");
        }
        ShellContext.cur().getJsonSqlContext().setTableConfig(tableName,key,value);
    }

    @CommandMethod(name = {"createTable"},desc = "新增表")
    public boolean createTable(@CommandParam(desc = "数据文件路径，包括文件名") String dataPath,
                            @CommandParam(desc = "表名") String tableName){
        if(ObjectUtil.isEmpty(dataPath)){
            throw new RuntimeException("数据文件不能为空");
        }
        if (!FileUtil.exist(dataPath) || !FileUtil.isFile(dataPath)) {
            throw new RuntimeException("数据文件不存在");
        }
        String data = FileUtil.readUtf8String(dataPath);
        if (!ShellContext.cur().getJsonSqlContext().isJsonData(data)) {
            throw new RuntimeException("数据不是json");
        }
        if (tablePathMap.containsKey(tableName)) {
            throw new RuntimeException("表名已存在");
        }
        if(ObjectUtil.isEmpty(tableName)){
            tableName = FileUtil.mainName(dataPath);
        }
        ShellContext.cur().getJsonSqlContext().registerTable(tableName,data);
        tablePathMap.put(tableName,dataPath);
        return true;
    }

}
