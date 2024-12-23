package tool.test;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import tool.GetBy;
import tool.ListBy;

//@Table(name = "aluno_table")
public class Aluno {
	
	@Id  //gera as operaões de CRUD normais para o campo id
	@GeneratedValue //nao vai atualizar este campo
	private int id;	
	
    @GetBy // gera uma operacao de consulta por cpf.
	private String cpf;
    
    @ListBy  // gera uma consulta de Listagem por nome
	private String nome;
    
	@Column(name="curso", updatable = false) //"updatable = false" -> Nao gera uma consulta de update para esta coluna.
	private String curso;
	
	@Column(name = "campus_name")
	private String campus;
	
    @GetBy //gera uma operacao de consulta por email
	private String email;
    
	private String senha;
	private String periodo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sys_creation_date")
    private Date  sysCreationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sys_update_date")
    private Date  sysUpdateDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		this.curso = curso;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public Date getSysCreationDate() {
		return sysCreationDate;
	}

	public void setSysCreationDate(Date sysCreationDate) {
		this.sysCreationDate = sysCreationDate;
	}

	public Date getSysUpdateDate() {
		return sysUpdateDate;
	}

	public void setSysUpdateDate(Date sysUpdateDate) {
		this.sysUpdateDate = sysUpdateDate;
	}
	
    
}
