# docs/ — índice documental

> Autoridade corrente: [`sot/V_WATCHER_SOURCE_OF_TRUTH.md`](sot/V_WATCHER_SOURCE_OF_TRUTH.md).
> Todo o resto é apêndice, método ou histórico. Em conflito, o SOT vence.

| Pasta | Conteúdo | Status |
|---|---|---|
| [`sot/`](sot/) | SOT canônico + matrizes de claims/componentes/provas + registro documental + consistência | **AUTORIDADE CORRENTE** |
| [`release/`](release/) | Gate, matriz, pacote (SHA), remediação, build, permissões, privacidade, Data Safety, rede, SDKs/licenças, segurança, prontidão Play/Console | Evidência de release (corrente) |
| [`../evidence/validation/`](../evidence/validation/) | Dados brutos e rastros de validação | Evidência reproduzível, não autoridade |
| [`../evidence/historical/`](../evidence/historical/) | Relatórios de validação superseded | Histórico — não usar como estado atual |
| [`../archive/petscan/`](../archive/petscan/) | Documentos do PET scan original | Método/histórico — superseded pelo SOT onde divergir |
| [`../archive/plans/`](../archive/plans/) | Planos e snapshots de implementação | Histórico de execução |
| [`reference/`](reference/) | Descoberta de ambiente, relatório da skill, brief de verificação externa | Referência pontual |

Regras: um claim de estado atual mora no SOT ou não existe; documento novo entra
numa pasta, nunca solto. Nada fora de `sot/` compete silenciosamente com o SOT.
