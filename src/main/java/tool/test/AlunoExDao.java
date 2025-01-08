
package tool.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import dao.NotFoundException;

public class AlunoExDao {

    private final static String getsql = "SELECT * FROM alunoex  WHERE id = ?";
    private final static String getByCpfSql = "SELECT * FROM alunoex  WHERE cpf = ?";
    private final static String getByEmailSql = "SELECT * FROM alunoex  WHERE email = ?";
    private final static String listsql = "SELECT * FROM alunoex";
    private final static String listByNomeSql = "SELECT * FROM alunoex WHERE nome = ? ";
    private final static String insertsql = "INSERT INTO alunoex (cpf, nome, curso, campus_name, email, senha, periodo, sys_creation_date, sys_update_date) VALUES( ?, ?, ?, ?, ?, ?, ?, sysdate(), sysdate()) ";
    private final static String updatesql = "UPDATE alunoex SET cpf = ?, nome = ?, campus_name = ?, email = ?, senha = ?, periodo = ?, sys_update_date = sysdate() WHERE id = ? ";
    private final static String updateForCpfSql = "UPDATE alunoex SET cpf = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForNomeSql = "UPDATE alunoex SET nome = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForCampusSql = "UPDATE alunoex SET campus_name = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForEmailSql = "UPDATE alunoex SET email = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForSenhaSql = "UPDATE alunoex SET senha = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String updateForPeriodoSql = "UPDATE alunoex SET periodo = ? , sys_update_date = sysdate()  WHERE id = ? ";
    private final static String deletesql = "DELETE FROM alunoex WHERE id = ?";

    private static void closeResource(Statement ps) {
        try{if (ps != null) ps.close();}catch (Exception e){ps = null;}
    }

    private static void closeResource(Statement ps, ResultSet rs) {
        try{if (rs != null) rs.close();}catch (Exception e){rs = null;}
        try{if (ps != null) ps.close();}catch (Exception e){ps = null;}
    }

    static AlunoEx set(ResultSet rs)
        throws SQLException
    {
        AlunoEx vo = new AlunoEx();
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

    public static AlunoEx get(Connection conn, int id)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getsql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {throw new NotFoundException("Object not found [" + id + "]");}
            AlunoEx b = set(rs);
            return b;
        }
        catch (SQLException e){throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static AlunoEx getByCpf(Connection conn, String cpf)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getByCpfSql);
            ps.setString(1, cpf);
            rs = ps.executeQuery();
            if (!rs.next()) {throw new NotFoundException("Object not found By [" + cpf + "]");}
            AlunoEx b = set(rs);
            return b;
        }
        catch (SQLException e){throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static AlunoEx getByEmail(Connection conn, String email)
        throws NotFoundException, SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(getByEmailSql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (!rs.next()) {throw new NotFoundException("Object not found By [" + email + "]");}
            AlunoEx b = set(rs);
            return b;
        }
        catch (SQLException e){throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static ArrayList<AlunoEx> list(Connection conn)
        throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(listsql);
            rs = ps.executeQuery();
            if (!rs.next()) {return new ArrayList<AlunoEx>();}
            ArrayList<AlunoEx> list = new ArrayList<AlunoEx>();
            do
            { AlunoEx b = set(rs); list.add(b); }
            while (rs.next());
            return list;
        }
        catch (SQLException e){ throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static ArrayList<AlunoEx> listByNome(Connection conn, String nome)
        throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(listByNomeSql);
            ps.setString(1, nome);
            rs = ps.executeQuery();
            if (!rs.next()) {return new ArrayList<AlunoEx>();}
            ArrayList<AlunoEx> list = new ArrayList<AlunoEx>();
            do
            { AlunoEx b = set(rs); list.add(b); }
            while (rs.next());
            return list;
        }
        catch (SQLException e){ throw e;}
        finally{closeResource(ps,rs); ps = null;rs = null; }
    }

    public static void insert(Connection conn, AlunoEx vo)
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

    public static void update(Connection conn, AlunoEx vo)
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
