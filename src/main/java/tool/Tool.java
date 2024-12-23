package tool;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import dao.NotFoundException;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;

public class Tool
{
    /**
     * Generate DAO form Bean representing the database table.
     * 
     * @param beanClass
     * 		Bean representing the database table.
     * @param sourcePath
     *		Path directory where DAO class will be generated 
     */
    public static void makeClass(Class<?> beanClass, String sourcePath) throws Exception
    {
        ArrayList<FieldInfo> fields = loadBean(beanClass);
        
        //Instantiate an instance of the JCodeModel class
        JCodeModel codeModel = new JCodeModel();

        //JDefinedClass will let you create a class in a specified package.
        JDefinedClass jcls = codeModel._class(beanClass.getCanonicalName() + "Dao");
//        JDefinedClass jcls = codeModel._class(
//        		NotFoundException.class.getPackageName()+
//        		"."+
//        		beanClass.getSimpleName() 
//        		+ "Dao");
        
        //jcls._extends(AbstractDao.class);

        createCloseResource1Method(codeModel, jcls);
        createCloseResource2Method(codeModel, jcls);
        
        createSetMethod(codeModel, jcls, beanClass, fields);
        createGetConnMethod(codeModel, jcls, beanClass, fields);

        for (FieldInfo f : fields)
        {
            if (f.isGetBy)
            {
                createGetByConnMethod(codeModel, jcls, beanClass, fields, f);
            }
        }
        
        createListConnMethod(codeModel, jcls, beanClass, fields);

        for (FieldInfo f : fields)
        {
            if (f.isListBy)
            {
                createListByConnMethod(codeModel, jcls, beanClass, fields, f);
            }
        }
        
        createInsertConnMethod(codeModel, jcls, beanClass, fields);

        createUpdateConnMethod(codeModel, jcls, beanClass, fields);

        for (FieldInfo f : fields)
        {
            if (f.isUpdateBy && f.isKey == false && !f.name.equals("sysUpdateDate") &&
               !f.name.equals("sysCreationDate") && !f.name.equals("sysCreateDate") )
            {
                createUpdateByConnMethod(codeModel, jcls, beanClass, fields, f);
            }
        }
        for (FieldInfo f : fields)
        {
            if (f.isKey == false && f.updatable == true 
                    && !f.name.equals("sysCreationDate") && !f.name.equals("sysCreateDate") 
                    && !f.name.equals("sysUpdateDate"))
            {
                createUpdateForConnMethod(codeModel, jcls, beanClass, fields, f);
            }
        }          
        
        createDeleteConnMethod(codeModel, jcls, beanClass, fields);

        for (FieldInfo f : fields)
        {
            if (f.isDeleteBy)
            {
                createDeleteByConnMethod(codeModel, jcls, beanClass, fields, f);
            }
        }  
        
        /* Building class at given location */
        codeModel.build(new File(sourcePath));
    }    

    private static ArrayList<FieldInfo> loadBean(Class<?> cls) throws Exception
    {
        ArrayList<FieldInfo> list = new ArrayList<FieldInfo>();

        Table table = cls.getAnnotation(Table.class);
        String tableName = table != null && table.name() != null && !table.name().equals("") ? table.name() : cls.getSimpleName().toLowerCase();

        Field fieldlist[] = cls.getDeclaredFields(); 

        for (int i = 0; i < fieldlist.length; i++)
        {
            FieldInfo info = new FieldInfo();

            Field fld = fieldlist[i];

            info.name = fld.getName();
            info.columnName = info.name;
            info.type = fld.getType();
            info.tableName = tableName;

            if (info.name.equals("serialVersionUID") ||
                    (info.type.isPrimitive() == false && 
                     info.type != String.class  &&
                     info.type.isEnum() == false && info.type != Date.class))
            {
                System.out.println("*** Nao eh tipo valido [" + info.name + "]");
                continue;
            }

            Id id = fld.getAnnotation(Id.class);
            GeneratedValue gv = fld.getAnnotation(GeneratedValue.class);
            Column col = fld.getAnnotation(Column.class);
            Temporal tp = fld.getAnnotation(Temporal.class);
            GetBy sf = fld.getAnnotation(GetBy.class);
            ListBy lf = fld.getAnnotation(ListBy.class);
            UpdateBy ubf = fld.getAnnotation(UpdateBy.class);
            DeleteBy df = fld.getAnnotation(DeleteBy.class);
            
            if (id != null)
            {
                info.isKey = true;
                if (gv != null)info.isKeyAutoGenerated = true;
            }
            if (col != null)
            {
                info.columnName = col.name() != null && col.name().equals("") == false ? col.name() : fld.getName();
                info.updatable = col.updatable();
                info.insertable = col.insertable();
            }
            if (tp != null)
            {
                info.tp = tp.value();
            }
            if (sf != null)
            {
                info.isGetBy = true;
            }
            if (lf != null)
            {
                info.isListBy = true;
            }
            if (ubf != null)
            {
                info.isUpdateBy = true;
                info.fieldToUpdate = cls.getDeclaredField(ubf.fieldToUpdate());
            }            
            if (df != null)
            {
                info.isDeleteBy = true;
            }
            
            list.add(info);

            System.out.println("name[" + info.name + "]  columnName[" + info.columnName + "] type[" + info.type + "]  isKey[" + info.isKey + "]  isKeyAutoGenerated["
                    + info.isKeyAutoGenerated + "]  updatable[" + info.updatable + "]  insertable[" + info.insertable + "]  tp[" + info.tp + "] isGetBy[" + info.isGetBy + "]  isUpdateBy[" + info.isUpdateBy + "] isListBy[" + info.isListBy + "] isDeleteBy[" + info.isDeleteBy + "]");
        }

        return list;
    }
    
    private static void createCloseResource2Method(JCodeModel codeModel, JDefinedClass cls) 
    throws Exception
    {
		/*
		    protected static void closeResource(Statement ps, ResultSet rs)
		    {
		        try
		        {
		            if (rs != null) rs.close();
		        }
		        catch (Exception e)
		        {
		            rs = null;
		        }
		
		        try
		        {
		            if (ps != null) ps.close();
		        }
		        catch (Exception e)
		        {
		            ps = null;
		        }
		    }
		 */
        JMethod met = cls.method(JMod.PRIVATE | JMod.STATIC, codeModel.VOID, "closeResource");
        met.param(codeModel.ref(Statement.class), "ps");
        met.param(codeModel.ref(ResultSet.class), "rs");
        JBlock block = met.body();
        block.directStatement("try{if (rs != null) rs.close();}catch (Exception e){rs = null;}" );
        block.directStatement("try{if (ps != null) ps.close();}catch (Exception e){ps = null;}" );        
    } 
    
    private static void createCloseResource1Method(JCodeModel codeModel, JDefinedClass cls) 
    throws Exception
    {
		/*
		    protected static void closeResource(Statement ps)
		    {
		        try
		        {
		            if (ps != null) ps.close();
		        }
		        catch (Exception e)
		        {
		            ps = null;
		        }
		    }
		 */
        JMethod met = cls.method(JMod.PRIVATE | JMod.STATIC, codeModel.VOID, "closeResource");
        met.param(codeModel.ref(Statement.class), "ps");
        JBlock block = met.body();
        block.directStatement("try{if (ps != null) ps.close();}catch (Exception e){ps = null;}" );  
    }     
    
    private static void createSetMethod(JCodeModel codeModel, JDefinedClass cls, Class<?> bean, ArrayList<FieldInfo> fields) throws Exception
    {
        /*
         static Video set(ResultSet rs) throws SQLException
         */
        JType ret = codeModel.parseType(bean.getCanonicalName());
        JType p1 = codeModel.parseType(ResultSet.class.getCanonicalName());
        //JMethod met = cls.method(JMod.PRIVATE | JMod.STATIC, ret, "set");
        JMethod met = cls.method(JMod.STATIC, ret, "set");
        met.param(p1, "rs");
        met._throws(SQLException.class);

        /*
        Bean vo = new Bean();
         */
        JBlock block = met.body();
        JClass vo = codeModel.ref(bean);
        JVar var = block.decl(vo, "vo");
        var.init(JExpr._new(vo));

        /*
        vo.setId(rs.getInt("id_video"));
        vo.setIdYoutube(rs.getString("id_youtube"));
        vo.setTitle(rs.getString("title"));
        vo.setMinutes(rs.getInt("minutes"));
        vo.setBlocks(rs.getInt("blocks"));
        vo.setBlocksReady(rs.getInt("blocks_ready"));
        vo.setSysCreationDate(rs.getTimestamp("sys_creation_date"));
        vo.setSysUpdateDate(rs.getTimestamp("sys_update_date"));
        
        vo.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));    
         */
        for (FieldInfo info : fields)
        {
            String stmt = null;
            
            if (info.type.isEnum())
            {
                stmt = "vo." + fCamelCase("set", info.name) +
                        "(" + info.type.getCanonicalName() + ".valueOf(rs.getString(\"" + info.columnName + "\").toUpperCase()));";
            }                 
            else
            {
                stmt = "vo." + fCamelCase("set", info.name) + "(rs." + fGetType(info) + "(\"" + info.columnName + "\"));";
            }                
                    
            /* Adding some direct statement */
            block.directStatement(stmt);
        }

        /*
        return vo;         
         */
        block._return(var);
    }


    private static void createGetConnMethod(JCodeModel codeModel, JDefinedClass cls, Class<?> bean, ArrayList<FieldInfo> fields) throws Exception
    {
        /*
         * Achar o campo de chave primaria
         */
        FieldInfo key = null;
        for (FieldInfo f : fields)
        {
            if (f.isKey)
            {
                key = f;
                break;
            }
        }

        if (key == null)
        {
            System.out.println("****  KEY NOT FOUND to create GET method. *****");
            return;
        }
        /*
         * SQL
         */
        String sql = "SELECT * FROM " + key.tableName + "  WHERE " + key.columnName + " = ?";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "getsql");
        getsql.init(JExpr.lit(sql));
        /*
            public static Video get(Connection conn, int id) throws NotFoundException, SQLException         
         */
        JType ret = codeModel.parseType(bean.getCanonicalName());
        JType p1 = codeModel.parseType(Connection.class.getCanonicalName());
        JType p2 = codeModel.parseType(key.type.getCanonicalName());
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, ret, "get");
        met.param(p1, "conn");
        met.param(p2, key.name);
        met._throws(NotFoundException.class);
        met._throws(SQLException.class);
        /*
        PreparedStatement ps = null;
        ResultSet rs = null;
         */
        JBlock block = met.body();

        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());

        JClass c_rs = codeModel.ref(ResultSet.class);
        JVar rs = block.decl(c_rs, "rs");
        rs.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(getsql);
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            if (!rs.next())
            {
                throw new NotFoundException("Object not found [" + id + "]");
            }
            
            Video u = set(rs);
            
            return u;
        }
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        tryblock.directStatement("ps = conn.prepareStatement(getsql);");
        tryblock.directStatement("ps." + fSetType(key) + "(1, " + key.name + ");");
        tryblock.directStatement("rs = ps.executeQuery();");
        tryblock.directStatement("if (!rs.next()) {throw new NotFoundException(\"Object not found [\" + " + key.name + " + \"]\");}");
        tryblock.directStatement(bean.getSimpleName()+" b = set(rs);");
        tryblock.directStatement("return b;");
        /*
        catch (SQLException e)
        {
            throw e;
        }
        finally
        {
            closeResource(ps,rs);
            ps = null;
            rs = null;
        }        
         */
        block.directStatement("catch (SQLException e){throw e;}");
        block.directStatement("finally{closeResource(ps,rs); ps = null;rs = null; }");
    }

    private static void createGetByConnMethod(JCodeModel codeModel, JDefinedClass cls, 
            Class<?> bean, ArrayList<FieldInfo> fields, FieldInfo searchField) 
    throws Exception
    {
        /*
         * SQL
         */
        String sql = "SELECT * FROM " + searchField.tableName + "  WHERE " + searchField.columnName + " = ?";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "getBy"+fCamelCase("", searchField.name)+"Sql");
        getsql.init(JExpr.lit(sql));
        /*
            public static Video getByFIELD(Connection conn, int fieldName) throws NotFoundException, SQLException         
         */
        JType ret = codeModel._ref(bean);
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, ret, "getBy"+fCamelCase("", searchField.name));
        met.param(codeModel._ref(Connection.class), "conn");
        met.param(codeModel._ref(searchField.type), searchField.name);
        met._throws(NotFoundException.class);
        met._throws(SQLException.class);
        /*
        PreparedStatement ps = null;
        ResultSet rs = null;
         */
        JBlock block = met.body();

        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());

        JClass c_rs = codeModel.ref(ResultSet.class);
        JVar rs = block.decl(c_rs, "rs");
        rs.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(getsql);
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            if (!rs.next())
            {
                throw new NotFoundException("Object not found [" + id + "]");
            }
            
            Video u = set(rs);
            
            return u;
        }
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        tryblock.directStatement("ps = conn.prepareStatement("+"getBy"+fCamelCase("", searchField.name)+"Sql"+");");
        if (searchField.type.isEnum() == false)
        {
            tryblock.directStatement("ps." + fSetType(searchField) + "(1, "+searchField.name+");");
        }
        else
        {
            tryblock.directStatement("ps.setString(1, "+searchField.name+".toString());");
        }
        tryblock.directStatement("rs = ps.executeQuery();");
        tryblock.directStatement("if (!rs.next()) {throw new NotFoundException(\"Object not found By [\" + " + searchField.name + " + \"]\");}");
        tryblock.directStatement(bean.getSimpleName()+" b = set(rs);");
        tryblock.directStatement("return b;");
        /*
        catch (SQLException e)
        {
            throw e;
        }
        finally
        {
            closeResource(ps,rs);
            ps = null;
            rs = null;
        }        
         */
        block.directStatement("catch (SQLException e){throw e;}");
        block.directStatement("finally{closeResource(ps,rs); ps = null;rs = null; }");
    }

    private static void createListConnMethod(JCodeModel codeModel, JDefinedClass cls, Class<?> beanClass, ArrayList<FieldInfo> fields) throws Exception
    {
        /*
         * SQL
         */
        String sql = "SELECT * FROM " + fields.get(0).tableName;
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "listsql");
        getsql.init(JExpr.lit(sql));
        /*
            public static ArrayList<Video> list(Connection conn)  throws SQLException      
         */
        JClass detailClass = codeModel.ref(beanClass);
        JClass rawLLclazz = codeModel.ref(ArrayList.class);
        JClass listClass = rawLLclazz.narrow(detailClass);

        JType p1 = codeModel.parseType(Connection.class.getCanonicalName());
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, listClass, "list");
        met.param(p1, "conn");
        met._throws(SQLException.class);
        /*
        PreparedStatement ps = null;
        ResultSet rs = null;
         */
        JBlock block = met.body();

        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());

        JClass c_rs = codeModel.ref(ResultSet.class);
        JVar rs = block.decl(c_rs, "rs");
        rs.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(listsql);
        
            rs = ps.executeQuery();
            if (!rs.next())
            {
                return new ArrayList<Video>();
            }
            
            ArrayList<Video> list = new ArrayList<Video>();
            do
            {
                Video o = set(rs);
                list.add(o);
            }
            while (rs.next());
            
            return list;
        }
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        tryblock.directStatement("ps = conn.prepareStatement(listsql);");
        tryblock.directStatement("rs = ps.executeQuery();");
        tryblock.directStatement("if (!rs.next()) {return new " + listClass.name() + "();}");
        tryblock.directStatement(listClass.name() + " list = new " + listClass.name() + "();");
        tryblock.directStatement("do");
        tryblock.directStatement("{ " + beanClass.getSimpleName() + " b = set(rs); list.add(b); }");
        tryblock.directStatement("while (rs.next());");
        tryblock.directStatement("return list;");
        /*
        catch (SQLException e)
        {
             throw e;
        }
        finally
        {
            closeResource(ps,rs);
            ps = null;
            rs = null;
        }        
         */
        block.directStatement("catch (SQLException e){ throw e;}");
        block.directStatement("finally{closeResource(ps,rs); ps = null;rs = null; }");
    }

    private static void createListByConnMethod(JCodeModel codeModel, JDefinedClass cls, 
            Class<?> beanClass, ArrayList<FieldInfo> fields, FieldInfo listField) 
    throws Exception
    {
        /*
         * SQL
         */
        String sql = "SELECT * FROM " + fields.get(0).tableName + " WHERE "+listField.columnName+" = ? ";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "listBy"+fCamelCase("", listField.name)+"Sql");
        getsql.init(JExpr.lit(sql));
        /*
            public static ArrayList<Video> list(Connection conn)  throws SQLException      
         */
        JClass detailClass = codeModel.ref(beanClass);
        JClass rawLLclazz = codeModel.ref(ArrayList.class);
        JClass listClass = rawLLclazz.narrow(detailClass);

        JType p1 = codeModel.parseType(Connection.class.getCanonicalName());
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, listClass, "listBy"+fCamelCase("", listField.name));
        met.param(p1, "conn");
        met.param(codeModel._ref(listField.type), listField.name);
        met._throws(SQLException.class);
        /*
        PreparedStatement ps = null;
        ResultSet rs = null;
         */
        JBlock block = met.body();

        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());

        JClass c_rs = codeModel.ref(ResultSet.class);
        JVar rs = block.decl(c_rs, "rs");
        rs.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(listsql);
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            if (!rs.next())
            {
                return new ArrayList<Video>();
            }
            
            ArrayList<Video> list = new ArrayList<Video>();
            do
            {
                Video o = set(rs);
                list.add(o);
            }
            while (rs.next());
            
            return list;
        }
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        tryblock.directStatement("ps = conn.prepareStatement(listBy"+fCamelCase("", listField.name)+"Sql);");
        if (listField.type.isEnum() == false)
        {
            tryblock.directStatement("ps." + fSetType(listField) + "(1, "+listField.name+");");
        }
        else
        {
            tryblock.directStatement("ps.setString(1, "+listField.name+".toString());");
        }        
        tryblock.directStatement("rs = ps.executeQuery();");
        tryblock.directStatement("if (!rs.next()) {return new " + listClass.name() + "();}");
        tryblock.directStatement(listClass.name() + " list = new " + listClass.name() + "();");
        tryblock.directStatement("do");
        tryblock.directStatement("{ " + beanClass.getSimpleName() + " b = set(rs); list.add(b); }");
        tryblock.directStatement("while (rs.next());");
        tryblock.directStatement("return list;");
        /*
        catch (SQLException e)
        {
             throw e;
        }
        finally
        {
            closeResource(ps,rs);
            ps = null;
            rs = null;
        }        
         */
        block.directStatement("catch (SQLException e){ throw e;}");
        block.directStatement("finally{closeResource(ps,rs); ps = null;rs = null; }");
    }

    private static void createDeleteByConnMethod(JCodeModel codeModel, JDefinedClass cls, 
            Class<?> beanClass, ArrayList<FieldInfo> fields, FieldInfo deleteField) 
    throws Exception
    {
        /*
         * SQL
         */
        String sql = "Delete FROM " + fields.get(0).tableName + " WHERE "+deleteField.columnName+" = ? ";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "deleteBy"+fCamelCase("", deleteField.name)+"Sql");
        getsql.init(JExpr.lit(sql));

        JType p1 = codeModel.parseType(Connection.class.getCanonicalName());
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, codeModel.VOID, "deleteBy"+fCamelCase("", deleteField.name));
        met.param(p1, "conn");
        met.param(codeModel._ref(deleteField.type), deleteField.name);
        met._throws(SQLException.class);
        /*
        PreparedStatement ps = null;
         */
        JBlock block = met.body();

        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());

        /*
        try
        {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
        }
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        tryblock.directStatement("ps = conn.prepareStatement(deleteBy"+fCamelCase("", deleteField.name)+"Sql);");
        if (deleteField.type.isEnum() == false)
        {
            tryblock.directStatement("ps." + fSetType(deleteField) + "(1, "+deleteField.name+");");
        }
        else
        {
            tryblock.directStatement("ps.setString(1, "+deleteField.name+".toString());");
        }        
        /*
            int count = ps.executeUpdate();
            
            if (count == 0 )
            {
                throw new NotFoundException("Object not found ["+id+"] .");
            }  
        }
         */
        tryblock.directStatement("int count = ps.executeUpdate();");
        tryblock.directStatement("if (count == 0 ){throw new NotFoundException(\"Object not found [\"+"+deleteField.name+"+\"] .\");}");
        /*
        catch (SQLException e)
        {
            try{conn.rollback();} catch (Exception e1){}
            throw e;
        }
        finally
        {
            closeResource(ps);
            ps = null;
        }     
         */
        block.directStatement("catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}");
        block.directStatement("finally{closeResource(ps); ps = null; }");

    }

    private static void createInsertConnMethod(JCodeModel codeModel, JDefinedClass cls, Class<?> beanClass, ArrayList<FieldInfo> fields) throws Exception
    {
        /*
         * Achar o campo de chave primaria
         */
        FieldInfo key = null;
        String fieldsNames = "";
        String fieldsValues = "";
        int count = 0;
        ArrayList<FieldInfo> insertList = new ArrayList<FieldInfo>();

        for (FieldInfo f : fields)
        {
            count++;

            if (f.isKey)
            {
                key = f;
            }
            if (f.name.equals("sysUpdateDate") || f.name.equals("sysCreationDate") || f.name.equals("sysCreateDate"))
            {
                fieldsNames += f.columnName;
                fieldsValues += "sysdate()";
            }
            else if (f.insertable && f.isKeyAutoGenerated == false)
            {
                fieldsNames += f.columnName;
                fieldsValues += "?";
                insertList.add(f);
            }
            if (count < fields.size() && f.isKeyAutoGenerated == false && f.insertable)
            {
                fieldsNames += ", ";
                fieldsValues += ", ";
            }
        }
        if (key == null)
        {
            System.out.println("****  KEY NOT FOUND to create INSERT method. *****");
            return;
        }
        /*
         * SQL
         */
        String sql = "INSERT INTO " + key.tableName + " (" + fieldsNames + ") VALUES( " + fieldsValues + ") ";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "insertsql");
        getsql.init(JExpr.lit(sql));
        /*
            public static void insert(Connection conn, Video vo) throws SQLException       
         */
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, codeModel.VOID, "insert");
        met.param(codeModel.ref(Connection.class), "conn");
        met.param(codeModel.ref(beanClass), "vo");
        met._throws(SQLException.class);
        /*
        PreparedStatement ps = null;
        ResultSet rs = null;
         */
        JBlock block = met.body();
        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());

        JClass c_rs = codeModel.ref(ResultSet.class);
        JVar rs = block.decl(c_rs, "rs");
        rs.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(insertsql, PreparedStatement.RETURN_GENERATED_KEYS);
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        if(key.isKeyAutoGenerated)
            tryblock.directStatement("ps = conn.prepareStatement(insertsql, PreparedStatement.RETURN_GENERATED_KEYS);");
        else
            tryblock.directStatement("ps = conn.prepareStatement(insertsql);");
        /*
            ps.setString(1, vo.getIdYoutube());
            ps.setString(2, vo.getTitle());
            ps.setString(3, vo.getStatus().toString());
            ps.setInt   (4, vo.getMinutes());
            ps.setInt   (5, vo.getBlocks());
            ps.setInt   (6, vo.getBlocksReady());
         */
        count = 0;
        for (FieldInfo f : insertList)
        {
            count++;
            if (f.type.isEnum())
            {
                tryblock.directStatement("ps.setString(" + count + ", vo." + fCamelCase("get", f.name) + "().toString());");
            }
            else
            {
                tryblock.directStatement("ps." + fSetType(f) + "(" + count + ", vo." + fCamelCase("get", f.name) + "());");
            }
        }
        /*
            ps.executeUpdate();
            
            rs = ps.getGeneratedKeys();
        
            if (rs.next()) 
            {
                int id = rs.getInt(1);
                vo.setId(id);
                
                //SEM COMMIT
                return id;
            } 
            else 
            {
                throw new SQLException("Nao foi possivel recuperar a CHAVE gerada na criacao do registro no banco de dados");
            }            
         */
        tryblock.directStatement("ps.executeUpdate();");
        
        if(key.isKeyAutoGenerated)
        {
            tryblock.directStatement("rs = ps.getGeneratedKeys();");
            tryblock.directStatement("if (rs.next()) {");
            tryblock.directStatement(key.type.getName() + " id = rs." + fGetType(key) + "(1);");
            tryblock.directStatement("vo." + fCamelCase("set", key.name) + "(id);");
            tryblock.directStatement("}else { throw new SQLException(\"Nao foi possivel recuperar a CHAVE gerada na criacao do registro no banco de dados\");} ");
        }
        /*
        catch (SQLException e)
        {
            try{conn.rollback();} catch (Exception e1){}
            throw e;
        }
        finally
        {
            closeResource(ps, rs);
            ps = null;
            rs = null;
        }      
         */
        block.directStatement("catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}");
        block.directStatement("finally{closeResource(ps,rs); ps = null;rs = null; }");
    }

    private static void createUpdateConnMethod(JCodeModel codeModel, JDefinedClass cls, Class<?> beanClass, ArrayList<FieldInfo> fields) throws Exception
    {
        /*
         * Achar o campo de chave primaria
         */
        FieldInfo key = null;
        String fieldsNames = "";
        int count = 0;
        ArrayList<FieldInfo> updateList = new ArrayList<FieldInfo>();
        for (FieldInfo f : fields)
        {
            count++;

            if (f.isKey)
            {
                key = f;
                continue;
            }
            else if (f.name.equals("sysCreationDate") || f.name.equals("sysCreateDate") || f.updatable == false)
            {
                continue;
            }
            else if (f.name.equals("sysUpdateDate"))
            {
                fieldsNames += f.columnName + " = sysdate()";
            }
            else
            {
                fieldsNames += f.columnName + " = ?";
                updateList.add(f);
            }

            if (count < fields.size()) //TODO mudar tatica. pode geraar erro dependendo da ordem dos campos
            {
                fieldsNames += ", ";
            }
        }
        if (key == null)
        {
            System.out.println("****  KEY NOT FOUND to create UPDATE method. *****");
            return;
        }
        /*   
         * SQL
         */
        String sql = "UPDATE " + key.tableName + " SET " + fieldsNames + " WHERE " + key.columnName + " = ? ";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "updatesql");
        getsql.init(JExpr.lit(sql));
        /*
            public static void update(Connection conn, Video vo) throws SQLException, NotFoundException      
         */
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, codeModel.VOID, "update");
        met.param(codeModel.ref(Connection.class), "conn");
        met.param(codeModel.ref(beanClass), "vo");
        met._throws(SQLException.class);
        met._throws(NotFoundException.class);
        /*
        PreparedStatement ps = null;
         */
        JBlock block = met.body();

        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(updatesql);
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();

        tryblock.directStatement("ps = conn.prepareStatement(updatesql);");
        /*
            ps.setString(1, vo.getIdYoutube());
            ps.setString(2, vo.getTitle());
            ps.setString(3, vo.getStatus().toString());
            ps.setInt   (4, vo.getMinutes());
            ps.setInt   (5, vo.getBlocks());
            ps.setInt   (6, vo.getBlocksReady());
         */
        count = 0;
        for (FieldInfo f : updateList)
        {
            count++;
            if (f.type.isEnum())
            {
                tryblock.directStatement("ps.setString(" + count + ", vo." + fCamelCase("get", f.name) + "().toString());");
            }        
            else
            {
                tryblock.directStatement("ps." + fSetType(f) + "(" + count + ", vo." + fCamelCase("get", f.name) + "());");
            }
        }
        /*
            ps.setInt   (7, vo.getId());
            int count = ps.executeUpdate();
            if (count == 0 )
            {
                throw new NotFoundException("Object not found ["+id+"] .");
            }  
            //SEM COMMIT         
         */
        count++;
        tryblock.directStatement("ps." + fSetType(key) + "(" + count + ", vo." + fCamelCase("get", key.name) + "());");
        tryblock.directStatement("int count = ps.executeUpdate();");
        tryblock.directStatement("if (count == 0 ){ throw new NotFoundException(\"Object not found [\"+ vo." + fCamelCase("get", key.name) + "()+\"] .\"); }");
        tryblock.directStatement("//SEM COMMIT ");
        /*
        catch (SQLException e)
        {
            try{conn.rollback();} catch (Exception e1){}
            throw e;
        }
        finally
        {
            closeResource(ps);
            ps = null;
        }     
         */
        block.directStatement("catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}");
        block.directStatement("finally{closeResource(ps); ps = null; }");
    }

    private static void createUpdateByConnMethod(JCodeModel codeModel, JDefinedClass cls, 
            Class<?> beanClass, ArrayList<FieldInfo> fields, FieldInfo updateByField) 
    throws Exception
    {
        /*
         * montar campos da query
         */
        String fieldsNames = updateByField.fieldToUpdate.getName() + " = ? ";
        if (updateByField.fieldToUpdate.getAnnotation(Column.class) != null)
            fieldsNames = updateByField.fieldToUpdate.getAnnotation(Column.class).name()  + " = ? ";
        
        for (FieldInfo f : fields)
        {
            if (f.name.equals("sysUpdateDate"))
            {
                fieldsNames += " , "+ f.columnName + " = sysdate()";
            }
        }
        
        String metName = "updateBy"+fCamelCase("", updateByField.name);
        
        /*   
         * SQL  update pedido set status = ? , sys_update_date = sysdate() where email = ? 
         */
        String sql = "UPDATE " + updateByField.tableName + " SET " + fieldsNames + " WHERE " + updateByField.columnName + " = ? ";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, metName+"Sql");
        getsql.init(JExpr.lit(sql));
        /*
            public static void updatebyFIELD(Connection conn, type byfield, type targetField) throws SQLException, NotFoundException      
         */
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, codeModel.VOID, metName);
        met.param(codeModel.ref(Connection.class), "conn");
        met.param(codeModel._ref(updateByField.type), updateByField.name);
        met.param(codeModel._ref(updateByField.fieldToUpdate.getType()), updateByField.fieldToUpdate.getName()); 
        met._throws(SQLException.class);
        met._throws(NotFoundException.class);
        /*
        PreparedStatement ps = null;
         */
        JBlock block = met.body();

        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(updatesql);
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();

        tryblock.directStatement("ps = conn.prepareStatement("+metName+"Sql);");
        /*
            ps.setString(1, vo.getIdYoutube());
            ps.setString(2, vo.getTitle());
            ps.setString(3, vo.getStatus().toString());
            ps.setInt   (4, vo.getMinutes());
            ps.setInt   (5, vo.getBlocks());
            ps.setInt   (6, vo.getBlocksReady());
         */

        if (updateByField.fieldToUpdate.getType().isEnum())
        {
            tryblock.directStatement("ps.setString(1, "+updateByField.fieldToUpdate.getName()+".toString());");
        }        
        else
        {
            tryblock.directStatement("ps." + fSetType(updateByField.fieldToUpdate.getType()) + "(1, "+updateByField.fieldToUpdate.getName()+");");            
        }
        
        if (updateByField.type.isEnum())
        {
            tryblock.directStatement("ps.setString(2, "+updateByField.name+".toString());");
        }      
        else
        {
            tryblock.directStatement("ps." + fSetType(updateByField) + "(2, "+updateByField.name+");");            
        }
        
        /*
            int count = ps.executeUpdate();
            if (count == 0 )
            {
                throw new NotFoundException("Object not found ["+id+"] .");
            }  
            //SEM COMMIT         
         */
        tryblock.directStatement("int count = ps.executeUpdate();");
        tryblock.directStatement("if (count == 0 ){ throw new NotFoundException(); }");
        tryblock.directStatement("//SEM COMMIT ");
        /*
        catch (SQLException e)
        {
            try{conn.rollback();} catch (Exception e1){}
            throw e;
        }
        finally
        {
            closeResource(ps);
            ps = null;
        }     
         */
        block.directStatement("catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}");
        block.directStatement("finally{closeResource(ps); ps = null; }");
    }

    private static void createUpdateForConnMethod(JCodeModel codeModel, JDefinedClass cls, 
            Class<?> beanClass, ArrayList<FieldInfo> fields, FieldInfo updateField) 
    throws Exception
    {
        String sysUpdateDate = "";
        for (FieldInfo f : fields)
        {
            if(f.name.equals("sysUpdateDate"))
            {
                sysUpdateDate = ", sys_update_date = sysdate() ";
                break;
            }
        }
        /*
         * Achar o campo de chave primaria
         */
        FieldInfo key = null;
        for (FieldInfo f : fields)
        {
            if (f.isKey)
            {
                key = f;
                break;
            }
        }
        if (key == null)
        {
            System.out.println("****  KEY NOT FOUND to create UPDATEFOR method. *****");
            return;
        }
        /*   
         * SQL
         */
        String sql = "UPDATE " + key.tableName + " SET " + updateField.columnName + " = ? "+sysUpdateDate+" WHERE " + key.columnName + " = ? ";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "updateFor"+fCamelCase("", updateField.name)+"Sql");
        getsql.init(JExpr.lit(sql));
        /*
            public static void updateForFIELLD(Connection conn, key, field) throws SQLException, NotFoundException      
         */
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, codeModel.VOID, "updateFor"+fCamelCase("", updateField.name));
        met.param(codeModel.ref(Connection.class), "conn");
        met.param(codeModel._ref(key.type), key.name);
        met.param(codeModel._ref(updateField.type), updateField.name);
        met._throws(SQLException.class);
        met._throws(NotFoundException.class);        
        /*
        PreparedStatement ps = null;
         */
        JBlock block = met.body();
        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(updateFor...sql);
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        tryblock.directStatement("ps = conn.prepareStatement(updateFor"+fCamelCase("", updateField.name)+"Sql);");
        /*
            ps.setString(1, fieldname);
         */
        if (updateField.type.isEnum())
        {
            tryblock.directStatement("ps.setString(1, "+updateField.name+".toString());");
        }      
        else
        {
            tryblock.directStatement("ps." + fSetType(updateField) + "(1, "+updateField.name+");");            
        }
        /*
            ps.setInt   (2, key);
            int count = ps.executeUpdate();
            if (count == 0 )
            {
                throw new NotFoundException("Object not found ["+id+"] .");
            }  
            //SEM COMMIT         
         */
        tryblock.directStatement("ps." + fSetType(key) + "(2, "+key.name+");");
        tryblock.directStatement("int count = ps.executeUpdate();");
        tryblock.directStatement("if (count == 0 ){ throw new NotFoundException(\"Object not found [\"+ "+key.name+" + \"] .\"); }");
        tryblock.directStatement("//SEM COMMIT ");
        /*
        catch (SQLException e)
        {
            try{conn.rollback();} catch (Exception e1){}
            throw e;
        }
        finally
        {
            closeResource(ps);
            ps = null;
        }     
         */
        block.directStatement("catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}");
        block.directStatement("finally{closeResource(ps); ps = null; }");
    }

    private static void createDeleteConnMethod(JCodeModel codeModel, JDefinedClass cls, Class<?> beanClass, ArrayList<FieldInfo> fields) throws Exception
    {
        /*
         * Achar o campo de chave primaria
         */
        FieldInfo key = null;
        for (FieldInfo f : fields)
        {
            if (f.isKey)
            {
                key = f;
                break;
            }
        }
        if (key == null)
        {
            System.out.println("****  KEY NOT FOUND to create DELETE method. *****");
            return;
        }
        /*
         * SQL
         */
        String sql = "DELETE FROM " + fields.get(0).tableName + " WHERE " + key.columnName + " = ?";
        JFieldVar getsql = cls.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "deletesql");
        getsql.init(JExpr.lit(sql));
        /*
            public static void delete(Connection conn, int id) throws SQLException, NotFoundException     
         */
        JMethod met = cls.method(JMod.PUBLIC | JMod.STATIC, codeModel.VOID, "delete");
        met.param(codeModel._ref(Connection.class), "conn");
        met.param(codeModel._ref(key.type), key.name);
        met._throws(SQLException.class);
        met._throws(NotFoundException.class);
        /*
        PreparedStatement ps = null;
         */
        JBlock block = met.body();
        JClass c_ps = codeModel.ref(PreparedStatement.class);
        JVar ps = block.decl(c_ps, "ps");
        ps.init(JExpr._null());
        /*
        try
        {
            ps = conn.prepareStatement(deletesql);
        
            ps.setInt(1, id);
            int count = ps.executeUpdate();
            
            if (count == 0 )
            {
                throw new NotFoundException("Object not found ["+id+"] .");
            }  
        }
         */
        JTryBlock trycatch = block._try();
        JBlock tryblock = trycatch.body();
        tryblock.directStatement("ps = conn.prepareStatement(deletesql);");
        tryblock.directStatement("ps." + fSetType(key) + "(1,"+key.name+");");
        tryblock.directStatement("int count = ps.executeUpdate();");
        tryblock.directStatement("if (count == 0 ){throw new NotFoundException(\"Object not found [\"+"+key.name+"+\"] .\");}");
        /*
        catch (SQLException e)
        {
            try{conn.rollback();} catch (Exception e1){}
            throw e;
        }
        finally
        {
            closeResource(ps);
            ps = null;
        }     
         */
        block.directStatement("catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}");
        block.directStatement("finally{closeResource(ps); ps = null; }");
    }

    private static String fCamelCase(String prefix, String name)
    {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static String fGetType(FieldInfo info)
    {
        if (info.type == int.class)
        {
            return "getInt";
        }
        if (info.type == long.class)
        {
            return "getLong";
        }
        if (info.type == short.class)
        {
            return "getShort";
        }        
        else if (info.type == String.class)
        {
            return "getString";
        }
        else if (info.type == Date.class)
        {
            return "getTimestamp";
        }
        else if (info.type == boolean.class)
        {
            return "getBoolean";
        }
        else if (info.type.isEnum())
        {
            return "getString";
        }

        return "****?????******";
    }

    private static String fSetType(FieldInfo info)
    {
        return fSetType(info.type);
    }

    private static String fSetType(Class<?> type)
    {
        if (type == int.class)
        {
            return "setInt";
        }
        else if (type == long.class)
        {
            return "setLong";
        }
        else if (type == short.class)
        {
            return "setShort";
        }   
        else if (type == boolean.class)
        {
            return "setBoolean";
        }         
        else if (type == String.class)
        {
            return "setString";
        }

        return "****?????******";
    }
}
