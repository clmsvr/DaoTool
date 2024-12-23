
package tool.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import dao.NotFoundException;

public class AlunoDao {

    private final static String getsql = "SELECT * FROM aluno  WHERE id = ?";
    private final static String getByCpfSql = "SELECT * FROM aluno  WHERE cpf = ?";
    private final static String getByEmailSql = "SELECT * FROM aluno  WHERE email = ?";
    private final static String listsql = "SELECT * FROM aluno";
    private final static String listByNomeSql = "SELECT * FROM aluno WHERE nome = ? ";
    private final static String insertsql = "INSERT INTO aluno (cpf, nome, curso, campus_name, email, senha, periodo, sys_creation_date, sys_update_date) VALUES( ?, ?, ?, ?, ?, ?, ?, sysdate(), sysdate()) ";
    private final static String updatesql = "UPDATE aluno SET cpf = ?, nome = ?, campus_name = ?, email = ?, senha = ?, periodo = ?, sys_update_date = sysdate() WHERE id = ? ";
    private final static String updateForCpfSql = "UPDATE aluno SET cpf = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForNomeSql = "UPDATE aluno SET nome = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForCampusSql = "UPDATE aluno SET campus_name = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForEmailSql = "UPDATE aluno SET email = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForSenhaSql = "UPDATE aluno SET senha = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForPeriodoSql = "UPDATE aluno SET periodo = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String deletesql = "DELETE FROM aluno WHERE id = ?";

    private static void closeResource(Statement ps) {
        try{if (ps != null) ps.close();}catch (Exception e){ps = null;}
    }

    private static void closeResource(Statement ps, ResultSet rs) {
        try{if (rs != null) rs.close();}catch (Exception e){rs = null;}
        try{if (ps != null) ps.close();}catch (Exception e){ps = null;}
    }

    static Aluno set(ResultSet rs)
        throws SQLException
    {
        Aluno vo = new Aluno();
        vo.setId(rs.getInt("id"));
        vo.setCpf(rs.getString("cpf"));
        vo.setNome(rs.getString("nome"));
        vo.setCurso(rs.getString("curso"));
        vo.setCampus(rs.getString("campus_name"));
        vo.setEmail(rs.getString("email"));
        vo.setSenha(rs.getString("senha"));
        vo.setPeriodo(rs.getString("periodo"));
        vo.setSysCreationDate(rs.getTimestamp("sys_creation_date"));
        vo.setSysUpdateDate(rs.getTimestamp("sys_update_date"));
        return vo;
    }

    public static Aluno get(Connection conn, int id)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getsql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {throw new NotFoundException("Object not found [" + id + "]");}
            Aluno b = set(rs);
            return b;
        }
        catch (SQLException e){throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static Aluno getByCpf(Connection conn, String cpf)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getByCpfSql);
            ps.setString(1, cpf);
            rs = ps.executeQuery();
            if (!rs.next()) {throw new NotFoundException("Object not found By [" + cpf + "]");}
            Aluno b = set(rs);
            return b;
        }
        catch (SQLException e){throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static Aluno getByEmail(Connection conn, String email)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getByEmailSql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (!rs.next()) {throw new NotFoundException("Object not found By [" + email + "]");}
            Aluno b = set(rs);
            return b;
        }
        catch (SQLException e){throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static ArrayList<Aluno> list(Connection conn)
        throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(listsql);
            rs = ps.executeQuery();
            if (!rs.next()) {return new ArrayList<Aluno>();}
            ArrayList<Aluno> list = new ArrayList<Aluno>();
            do
            { Aluno b = set(rs); list.add(b); }
            while (rs.next());
            return list;
        }
        catch (SQLException e){ throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static ArrayList<Aluno> listByNome(Connection conn, String nome)
        throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(listByNomeSql);
            ps.setString(1, nome);
            rs = ps.executeQuery();
            if (!rs.next()) {return new ArrayList<Aluno>();}
            ArrayList<Aluno> list = new ArrayList<Aluno>();
            do
            { Aluno b = set(rs); list.add(b); }
            while (rs.next());
            return list;
        }
        catch (SQLException e){ throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static void insert(Connection conn, Aluno vo)
        throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(insertsql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, vo.getCpf());
            ps.setString(2, vo.getNome());
            ps.setString(3, vo.getCurso());
            ps.setString(4, vo.getCampus());
            ps.setString(5, vo.getEmail());
            ps.setString(6, vo.getSenha());
            ps.setString(7, vo.getPeriodo());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
            int id = rs.getInt(1);
            vo.setId(id);
            }else { throw new SQLException("Nao foi possivel recuperar a CHAVE gerada na criacao do registro no banco de dados");} 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static void update(Connection conn, Aluno vo)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updatesql);
            ps.setString(1, vo.getCpf());
            ps.setString(2, vo.getNome());
            ps.setString(3, vo.getCampus());
            ps.setString(4, vo.getEmail());
            ps.setString(5, vo.getSenha());
            ps.setString(6, vo.getPeriodo());
            ps.setInt(7, vo.getId());
            int count = ps.executeUpdate();
            if (count == 0 ){ throw new NotFoundException("Object not found ["+ vo.getId()+"] ."); }
            //SEM COMMIT 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

    public static void updateForCpf(Connection conn, int id, String cpf)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updateForCpfSql);
            ps.setString(1, cpf);
            ps.setInt(2, id);
            int count = ps.executeUpdate();
            if (count == 0 ){ throw new NotFoundException("Object not found ["+ id + "] ."); }
            //SEM COMMIT 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

    public static void updateForNome(Connection conn, int id, String nome)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updateForNomeSql);
            ps.setString(1, nome);
            ps.setInt(2, id);
            int count = ps.executeUpdate();
            if (count == 0 ){ throw new NotFoundException("Object not found ["+ id + "] ."); }
            //SEM COMMIT 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

    public static void updateForCampus(Connection conn, int id, String campus)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updateForCampusSql);
            ps.setString(1, campus);
            ps.setInt(2, id);
            int count = ps.executeUpdate();
            if (count == 0 ){ throw new NotFoundException("Object not found ["+ id + "] ."); }
            //SEM COMMIT 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

    public static void updateForEmail(Connection conn, int id, String email)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updateForEmailSql);
            ps.setString(1, email);
            ps.setInt(2, id);
            int count = ps.executeUpdate();
            if (count == 0 ){ throw new NotFoundException("Object not found ["+ id + "] ."); }
            //SEM COMMIT 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

    public static void updateForSenha(Connection conn, int id, String senha)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updateForSenhaSql);
            ps.setString(1, senha);
            ps.setInt(2, id);
            int count = ps.executeUpdate();
            if (count == 0 ){ throw new NotFoundException("Object not found ["+ id + "] ."); }
            //SEM COMMIT 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

    public static void updateForPeriodo(Connection conn, int id, String periodo)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(updateForPeriodoSql);
            ps.setString(1, periodo);
            ps.setInt(2, id);
            int count = ps.executeUpdate();
            if (count == 0 ){ throw new NotFoundException("Object not found ["+ id + "] ."); }
            //SEM COMMIT 
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

    public static void delete(Connection conn, int id)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(deletesql);
            ps.setInt(1,id);
            int count = ps.executeUpdate();
            if (count == 0 ){throw new NotFoundException("Object not found ["+id+"] .");}
        }
        catch (SQLException e){try{conn.rollback();} catch (Exception e1){}; throw e;}
        finally{closeResource(ps); ps = null; }
    }

}
