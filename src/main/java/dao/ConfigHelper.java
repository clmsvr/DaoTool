package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Properties;


public class ConfigHelper
{
    /*
        ATENCAO: se as propriedades s seguir nao forem definidas, o framework 
          ira buscar os arquivos no CLASSPATH da aplicacao. 
    */ 
    
    /**
        cms.web.CONFIG_DIR : 
         (opcional) indica o diretorio onde se encontram os arquivos de 
             configuracao da aplicacao. 
    */    
    public static String CONFIG_DIR = 
        "cms.web.CONFIG_DIR";
    /**
       cms.web.SYSTEM_PROPERY_TO_CONFIG_DIR :
        (opcional) indica o nome da propriedade usada na inicializacao
         do servidor (jvm) que indica diretorio onde se encontram os 
         arquivos de configuracao da aplicacao. 
    */      
    public static String SYSTEM_PROPERY_TO_CONFIG_DIR = 
        "cms.web.SYSTEM_PROPERY_TO_CONFIG_DIR";
    

    /**
     * Informa o Diretorio de configura��o (CONFIG_DIR), se definido na aplica��o.
     * @return
     */
    public static String getConfigDir()
    {
        String prop = System.getProperty(CONFIG_DIR);
        if (prop == null || prop.trim().equals(""))
        {
            return null;
        }
        return prop;
    }
    
    /**
     * Obtem uma InputStream para arquivo localizado no diretorio de 
     * configura��o da aplicacao (CONFIG_DIR).
     * 
     * Se o parametro 'searchInClassPath' for 'true' o classpath da aplica��o
     * ser� tamb�m usado na busca caso o arquivo n�o seja encotrado no 
     * diretorio de configura��o da aplica��o (CONFIG_DIR).
     * 
     * @param fileName
     * @param searchInClassPath  Se 'true' o classpath da aplica��o ser� tamb�m usado na busca.
     * @return
     * @throws IOException
     */
    public static InputStream getConfigFileStream(String fileName, boolean searchInClassPath) 
    throws IOException
    {
        String configDir = getConfigDir();
        if (configDir == null && !searchInClassPath)
        {
            throw new IOException("CONFIG_DIR not defined in application.");
        }
        if (configDir != null)
        {
            try
            {
                return new FileInputStream(new File(configDir,fileName));
            }
            catch (FileNotFoundException e)
            {
                if (!searchInClassPath) throw e;
                return getClassPathFileStrem(fileName);
            }
        }
        else
        {
            return getClassPathFileStrem(fileName);
        }
    }
    
    /**
     * Obtem as 'Properties' de um arquivo de propriedades localizado no 
     * diretorio de configuraao da aplicacao (CONFIG_DIR).
     * 
     * Se o parametro 'searchInClassPath' for 'true' o classpath da aplicacao
     * sera tambem usado na busca caso o arquivo nao seja encotrado no 
     * diretorio de configuraaoo da aplicaaoo (CONFIG_DIR).
     * 
     * @param fileName
     * @param searchInClassPath  Se 'true' o classpath da aplicaaoo ser� tambem usado na busca.
     * @return
     * @throws IOException
     */
    public static Properties getConfigFileProperties(String fileName, boolean searchInClassPath) 
    throws IOException
    {
        String configDir = getConfigDir();
        if (configDir == null && !searchInClassPath)
        {
            throw new IOException("CONFIG_DIR not defined in application.");
        }
        if (configDir != null)
        {
            try
            {
                InputStream is =  new FileInputStream(new File(configDir,fileName));
                Properties props = new Properties();
                try
                {
                    props.load(is);
                }
                finally
                {
                    try{if(is!=null)is.close();}catch (java.io.IOException e){} 
                }                
                return props;            
            }
            catch (FileNotFoundException e)
            {
                if (!searchInClassPath) throw e;
                return getClassPathFileProperties(fileName);
            }
        }
        else
        {
            return getClassPathFileProperties(fileName);
        }
    }
    
    public static InputStream getClassPathFileStrem(String filename)
    throws java.io.IOException
    {
        InputStream is = getClassPathResource(filename);
        if (is != null)
        {
            return is;
        }
        
        is = getClassPathResource(filename+".path");
        
        if (is == null)
        {
            throw new FileNotFoundException("Files not found in CLASSPATH: ["+filename+"] ["+filename+".path] ");
        }
        
        BufferedReader binp = new BufferedReader(new InputStreamReader(is));
        String filePath = binp.readLine();       
        try{binp.close();}catch (Exception e){} 
        
        if (filePath == null || filePath.equals(""))
        {
            throw new IOException("Path not found in file: "+filename+".path");
        }
        
        filePath = formatPath(filePath);
        
        FileInputStream fin = new FileInputStream(filePath);
        
        return fin;
    }

    public static Properties getClassPathFileProperties(String filename)
    throws java.io.IOException
    {
        InputStream is = getClassPathResource(filename);
        if (is != null)
        {
            Properties props = new Properties();
            try
            {
                props.load(is);
            }
            finally
            {
                try{if(is!=null)is.close();}catch (java.io.IOException e){} 
            }             
            return props;
        }
        
        is = getClassPathResource(filename+".path");
        
        if (is == null)
        {
            throw new FileNotFoundException("Files not found in CLASSPATH: ["+filename+"] ["+filename+".path] ");
        }
        
        BufferedReader binp = new BufferedReader(new InputStreamReader(is));
        String filePath;      
        try
        {
            filePath = binp.readLine(); 
        }
        finally
        {
            try{if(binp!=null)binp.close();}catch (Exception e){} 
        } 
        
        if (filePath == null || filePath.equals(""))
        {
            throw new IOException("Path not found in file: "+filename+".path");
        }
        
        filePath = formatPath(filePath);
        
        FileInputStream fin = new FileInputStream(filePath);
        
        Properties props = new Properties();
        try
        {
            props.load(fin);
        }
        finally
        {
            try{if(fin!=null)fin.close();}catch (Exception e){}
        } 
        return props;
    }

    private static String formatPath(String filePath)
    {
        String fsep = System.getProperty("file.separator", "/");        

        if (filePath.indexOf('/') >= 0 &&
            fsep.equals("\\"))
        {
            filePath = filePath.replace('/','\\');
        }
        else if (filePath.indexOf('\\') >= 0 &&
                 fsep.equals("/"))
        {
            if (filePath.substring(1).startsWith(":\\"))
            {
                filePath = filePath.substring(2);
            }
            filePath = filePath.replace('\\','/');
        }
        return filePath;
    }

    private static InputStream getClassPathResource(String name)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null)
        {
            InputStream input = classLoader.getResourceAsStream(name);
            if (input != null)return input;
        }
        
        classLoader = ConfigHelper.class.getClassLoader();
        InputStream input = classLoader.getResourceAsStream(name);
        
//        if (input == null)
//        {
//            input = InitializationHelper.class.getResourceAsStream(name);
//        }
        return input;
    }
    
    protected static String loadTextFile(InputStream is, boolean close) 
    throws IOException
    {
        BufferedReader bin = null;
        try
        {
            bin = new BufferedReader(new InputStreamReader(is));  
            String file = "";      
            String line = bin.readLine();
            while (line != null)
            {
                file += line + "\n";
                line = bin.readLine();
            }  
            return file;
        }
        finally
        {
            if (close)
            {
                if (bin != null) try{bin.close();}catch(Exception e){}
            }
        }
    }
    
    
    
    
    public static String decode64(String s)
    {
        return new String(Base64.getDecoder().decode(s));
    }
    
    public static void encode64(String propertiesFile, String prop) 
    throws IOException
    {
        StringWriter sw = new StringWriter();
        
        InputStream is = null;
        try
        {
            is =  new FileInputStream(new File(propertiesFile));
            Properties props = new Properties();
            props.load(is);
            props.put(prop, Base64.getEncoder().encodeToString(props.getProperty(prop).getBytes()));
            props.store(sw, "");
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            try{if(is!=null)is.close();}catch (java.io.IOException e){}
            if (sw != null)sw.close();
        }

        BufferedWriter bw = null;
        try
        {
            bw = new BufferedWriter(new FileWriter(new File(propertiesFile)));
            bw.write(sw.getBuffer().toString());
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (bw != null)bw.close();
        }
    }
    
    public static void main(String[] args)
    {
        System.out.println(Base64.getEncoder().encodeToString("sud5273&*".getBytes()));
        System.out.println(new String(Base64.getDecoder().decode("cHZkbSYxOTk4Kg==".getBytes())));
    }
}
