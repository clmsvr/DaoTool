package tool.test;

import tool.Tool;

public class Test {
    public static void main(String args[])
    {
        try
        {
            //Class<?> cls = Class.forName("tool.test.Aluno");
            //Class<?> cls = Class.forName("tool.test.UserTest");
            //makeClass(cls, "./src/main/java");
        	
        	Tool.makeClass(Aluno.class, "./src/main/java");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}