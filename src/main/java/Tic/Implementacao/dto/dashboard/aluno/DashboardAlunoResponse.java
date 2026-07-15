package Tic.Implementacao.dto.dashboard.aluno;

import Tic.Implementacao.dto.financeiro.preferencia.ResumoPreferenciaPagamentoResponse;

import java.util.List;

public class DashboardAlunoResponse {
    private Long alunoId;
    private String nome;
    private Boolean ativo;
    private ResumoPrajiedResponse prajied;
    private ResumoCheckInResponse checkIns;
    private ResumoPlanoAlunoResponse plano;
    private ResumoFinanceiroAlunoResponse financeiro;
    private ResumoPreferenciaPagamentoResponse preferenciaPagamento;
    private ResumoPesoDashboardResponse evolucaoPeso;
    private ProgressoMetaPesoDashboardResponse metaPeso;
    private ResumoAvaliacaoCorporalDashboardResponse avaliacaoCorporal;
    private ProximaAulaAlunoResponse proximaAula;
    private List<AulaAlunoResponse> aulasHoje;
    private List<AulaAlunoResponse> aulasSemana;
    private List<HorarioPersonalAlunoResponse> horariosFixosPersonal;

    public DashboardAlunoResponse(Long alunoId, String nome, Boolean ativo, ResumoPrajiedResponse prajied,
                                  ResumoCheckInResponse checkIns, ResumoPlanoAlunoResponse plano,
                                  ResumoFinanceiroAlunoResponse financeiro, ResumoPesoDashboardResponse evolucaoPeso,
                                  ResumoPreferenciaPagamentoResponse preferenciaPagamento,
                                  ProgressoMetaPesoDashboardResponse metaPeso,
                                  ResumoAvaliacaoCorporalDashboardResponse avaliacaoCorporal,
                                  ProximaAulaAlunoResponse proximaAula,
                                  List<AulaAlunoResponse> aulasHoje, List<AulaAlunoResponse> aulasSemana,
                                  List<HorarioPersonalAlunoResponse> horariosFixosPersonal) {
        this.alunoId = alunoId;
        this.nome = nome;
        this.ativo = ativo;
        this.prajied = prajied;
        this.checkIns = checkIns;
        this.plano = plano;
        this.financeiro = financeiro;
        this.preferenciaPagamento = preferenciaPagamento;
        this.evolucaoPeso = evolucaoPeso;
        this.metaPeso = metaPeso;
        this.avaliacaoCorporal = avaliacaoCorporal;
        this.proximaAula = proximaAula;
        this.aulasHoje = aulasHoje;
        this.aulasSemana = aulasSemana;
        this.horariosFixosPersonal = horariosFixosPersonal;
    }

    public Long getAlunoId() { return alunoId; }
    public String getNome() { return nome; }
    public Boolean getAtivo() { return ativo; }
    public ResumoPrajiedResponse getPrajied() { return prajied; }
    public ResumoCheckInResponse getCheckIns() { return checkIns; }
    public ResumoPlanoAlunoResponse getPlano() { return plano; }
    public ResumoFinanceiroAlunoResponse getFinanceiro() { return financeiro; }
    public ResumoPreferenciaPagamentoResponse getPreferenciaPagamento() { return preferenciaPagamento; }
    public ResumoPesoDashboardResponse getEvolucaoPeso() { return evolucaoPeso; }
    public ProgressoMetaPesoDashboardResponse getMetaPeso() { return metaPeso; }
    public ResumoAvaliacaoCorporalDashboardResponse getAvaliacaoCorporal() { return avaliacaoCorporal; }
    public ProximaAulaAlunoResponse getProximaAula() { return proximaAula; }
    public List<AulaAlunoResponse> getAulasHoje() { return aulasHoje; }
    public List<AulaAlunoResponse> getAulasSemana() { return aulasSemana; }
    public List<HorarioPersonalAlunoResponse> getHorariosFixosPersonal() { return horariosFixosPersonal; }
}
