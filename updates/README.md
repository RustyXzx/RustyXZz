# Canal automático de atualizações RustyX

O aplicativo consulta `latest.json`, compara a versão instalada e exibe as novidades no sininho fixo da Home.

A versão 5.5.59 é a base local do atualizador. Versões posteriores são publicadas como patches cumulativos criptografados. Antes de aplicar qualquer atualização, o painel valida HTTPS, SHA-256, integridade criptográfica e mantém uma cópia do executável anterior para restauração automática.
