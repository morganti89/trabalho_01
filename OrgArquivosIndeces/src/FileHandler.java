import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class FileHandler {

    private File arquivo_original = null;
    private final String caminho_raiz = "C:\\Users\\junior\\Desktop\\faculdade\\semestre 7\\algorimo e estrutura de dados 2\\trabalho\\arquivo\\";
    private final String caminho_original = "C:\\Users\\junior\\Desktop\\faculdade\\semestre 7\\algorimo e estrutura de dados 2\\trabalho\\arquivo\\mock.csv";
    private String caminho_binario;
    private String cabecalho;
    private Dados dados;
    ArrayList<Dados> listaDados = new ArrayList<Dados>();
    private HashMap <String, Integer> tamanhoDados = new HashMap();

    public Dados getDados() {
        return dados;
    }

    public FileHandler(){
        this.arquivo_original = new File(this.caminho_original);
    }

    //metodo para criar dinamicamente os indices no arquivo csv
    public void criaArquivoIndexado(String caminho_original, String caminhoFinal) throws FileNotFoundException {
        String[] c = caminhoFinal.split("_");
        String chave = c[0];
        String caminhoAbsoluto = this.caminho_raiz + caminhoFinal;
        if(verificaArquivo(caminhoAbsoluto)) {
            return;
        }
        //passa o arquivo original
        BufferedReader buffReader = new BufferedReader(new FileReader(this.caminho_raiz + caminho_original));

        String linha = "";
        try {
            //le o arquivo
            while((linha = buffReader.readLine()) != null) {
                if(linha.equals("ID,job,e-mail,name,city")){
                    continue;
                }
                String[] tmp = linha.split(",");
                dados = new Dados(tmp);
                dados.setChave(chave);
                listaDados.add(dados);
            }
            //organiza o arquivo
            listaDados.sort((a,b) -> (a.compareTo(b)));

            //escreve em um novo arquivo
            FileWriter fw = new FileWriter(caminhoAbsoluto, true);
            int i = 0;
            String dadoCsv = "";
            if(i == 0){
                dadoCsv = chave+",ID";
                fw.append(dadoCsv);
                fw.append("\n");
            }
            for (Dados _d: listaDados) {
                String s = Integer.toString(i);
                if(chave.equals("name")) {
                    dadoCsv = _d.getName()+","+_d.getId();
                } else if (chave.equals("e-mail")) {
                    dadoCsv = _d.getEmail()+","+_d.getId();
                }
                fw.append(dadoCsv);
                fw.append("\n");
                i++;
            }
            fw.flush();
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //metodo para criar o arquivo binario temporario para fazer pesquisa
    public void criaArquivoBinario(String arquivoOriginal, String arquivoFinal) throws IOException {
        String _arquivoOriginal = this.caminho_raiz+arquivoOriginal;
        //le do arquivo indexado
        BufferedReader buffReader = new BufferedReader(new FileReader(_arquivoOriginal));

        String rowReader = "";
        this.caminho_binario = this.caminho_raiz+arquivoFinal;
        if(verificaArquivo(this.caminho_binario)) {
            rowReader = buffReader.readLine();
            insereTamanhoCabecalho(rowReader);
            this.cabecalho = rowReader;
            return;
        }

        //grava no arquivo binario
        BufferedWriter buffWriter = new BufferedWriter(new FileWriter(this.caminho_binario));

        while (true) {
            rowReader = buffReader.readLine();
            int tamanhoCabecalho = 0;
            if (rowReader != null) {
                if(rowReader.indexOf("ID") != -1) {
                    insereTamanhoCabecalho(rowReader);
                    this.cabecalho = rowReader;
                    continue;
                }
                String s = "";
                try {
                    s = normalizaTextoParaBinario(rowReader, tamanhoCabecalho);
                } catch (Exception e) {
                    System.out.println(e);
                }
                if(rowReader != "")
                    buffWriter.write(s);
            } else
                break;
        }
        buffReader.close();
        buffWriter.close();
    }

    public int buscaBinariaIDPorNome(String procura) throws IOException {
        String pr = removeEspacosEmBrancoFinal(converteTextoBinario(procura));
        String resposta = buscaBinaria(pr, "name");
        String texto = converteBinarioTexto(resposta);
        return Integer.parseInt(removeEspacosEmBrancoFinal(texto));
    }

    public int buscaBinariaIDPorEmail(String procura) throws IOException {
        String pr = removeEspacosEmBrancoFinal(converteTextoBinario(procura));
        String resposta = buscaBinaria(pr, "e-mail");
        String texto = converteBinarioTexto(resposta);
        return Integer.parseInt(removeEspacosEmBrancoFinal(texto));
    }

    public String[] buscaLinhaBinariaPorId(int procura)  throws IOException {
        String p = Integer.toString(procura);
        String pr = removeEspacosEmBrancoFinal(converteTextoBinario(p));
        String busca = buscaBinaria(pr, "ID");

        String[] cabecalho = this.cabecalho.split(",");
        String[] resposta = new String[cabecalho.length];
        int tamanhoIn = 0;
        int i = 0;
        for (String cab: cabecalho) {
            int tamanhoFim = this.tamanhoDados.get(cab) + tamanhoIn;
            String idx = busca.substring(tamanhoIn*8, tamanhoFim*8);
            idx = removeEspacosEmBrancoFinal(idx);
            String[] tmp = idx.split(" ");
            String resp = "";
            for (String _tmp: tmp) {
                int charCode = Integer.parseInt(_tmp, 2);
                resp +=  Character.valueOf((char)charCode).toString();
            }
            resposta[i++] =  resp;
            tamanhoIn = tamanhoFim + 1;
        }

        dados = new Dados(resposta);
        return resposta;
    }

    //RETORNA O DADO EM BUNARIO
    private String buscaBinaria(String pr, String tipo) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(this.caminho_binario));
        //tamanho total do arquivo
        int fileLength = 1;
        while((in.read()) != -1){
            fileLength++;
        }

        //verifica o cabecalho <- inicializado quando cria o arquivo binario
        String[] cabecalho = this.cabecalho.split(",");
        int tamanho = 0;
        int c = 0; //contador de espaçoes
        for (String cab: cabecalho) {
            tamanho += this.tamanhoDados.get(cab);
            c++;
        }
        tamanho += c;

        //calcula o salto pelo indice do cabecalho (hashmap)
        int _tipo = this.tamanhoDados.get(tipo);

        RandomAccessFile raf = new RandomAccessFile(this.caminho_binario, "r");
        boolean achou = false;
        int inicio = 0;
        int fim = fileLength;
        while(!achou) {
            if((fim - inicio) > 0) {
                int meio = ((inicio + fim) / 2) - tamanho*8;

                if(meio % (tamanho*8) > 0) {
                    meio += (tamanho*8)/2;
                }

                raf.seek(meio);
                String str = "";

                for(int i = 0; i < _tipo*8; i++) {
                    int h = raf.read();
                    str +=  Character.valueOf((char)h).toString();
                }
                String st = removeEspacosEmBrancoFinal(str);
                if(pr.compareTo(st) == 0) {
                    String resposta = "";
                    raf.seek(meio);
                    String linha = "";
                    for(int i = 0; i < tamanho*8; i++) {
                        int h = raf.read();
                        linha +=  Character.valueOf((char)h).toString();
                    }
                    //separa somente a linha
                    String idx = "";
                    if(tipo.equals("ID")) {
                        //pega toda a linha (do começo ao final)
                        idx = linha.substring(0, tamanho * 8);
                    } else {
                        //pega a linha a partir do tamanho do tipo
                        idx = linha.substring((this.tamanhoDados.get(tipo) + 1)*8, tamanho * 8);
                    }
                    return idx;

                } else if(pr.compareTo(st) < 0) {
                    //menor
                    fim = meio + 2*(tamanho*8);
                } else if (pr.compareTo(st) > 0) {
                    //maior
                    inicio = meio;
                }
            }
        }
        return "";
    }

    //NORMALIZA OS DADOS TEXTO PARA REALIZAR A CONVERSÃO
    private String normalizaTextoParaBinario(String input, int tamanhoCabecalho) {

        StringBuilder result = new StringBuilder();
        String[] tmp = input.split(",");
        String[] cabecalho = this.cabecalho.split(",");
        String str = "";
        //NAME

        for (int i = 0; i < tmp.length; i++) {
            char[] c= this.colocaEspacoEmBranco(tmp[i], this.tamanhoDados.get(cabecalho[i]));
            tmp[i] = String.valueOf(c);
        }

        //junta novamente a linha
        String _tmp[] = new String[tmp.length];
        for (int i = 0; i < _tmp.length; i++) {
            _tmp[i] = tmp[i];
        }
        str = String.join(" ", _tmp);
        str += " ";
        return converteTextoBinario(str);
    }

    //CONVERSÃO DO TEXTO PARA BINARIO
    private String converteTextoBinario(String s) {
        StringBuilder result = new StringBuilder();
        char[] chars = s.toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar))                                         // char -> int, auto-cast
                            .replaceAll(" ", "0").replaceAll("-", "0")         // zero pads
            );
        }
        return result.toString();
    }

    //SEPARA EM GRUPOS DE OITO A STRING BINARIA
    public static String prettyBinary(String binary, int blockSize, String separator) {

        List<String> result = new ArrayList<>();
        int index = 0;
        while (index < binary.length()) {
            result.add(binary.substring(index, Math.min(index + blockSize, binary.length())));
            index += blockSize;
        }

        return result.stream().collect(Collectors.joining(separator));
    }

    //REMOVE OS ESPAÇOS EM BRANCO PARA REALIZAR A COMPARAÇÃO NO FINAL DA STRING
    private String removeEspacosEmBrancoFinal(String binarioComEspaco) {
        String s = prettyBinary(binarioComEspaco,8, " ");
        String[] tmp = s.split(" ");
        int c = 0;
        for (int i = tmp.length - 1; i >= 0; i--) {
            if(tmp[i].equals("00100000")){
                tmp[i] = null;
                c++;
            } else  if(!tmp[i].equals("00100000"))
                break;
        }
        String _tmp[] = new String[tmp.length - c];
        for (int i = 0; i < tmp.length; i++) {
            if(tmp[i] == null)
                break;
            _tmp[i] = tmp[i];
        }
        String str = String.join(" ", _tmp);
        return str;
    }

    private String removeEspacaoEmBrancoBinario(String binarioComEspaco) {
        String s = prettyBinary(binarioComEspaco,8, " ");
        String[] tmp = s.split(" ");
        int c = 0;
        for (int i = 0; i < tmp.length; i++) {
            if(tmp[i].equals("00100000")){
                tmp[i] = null;
            } else  if(!tmp[i].equals("00100000")) {
                c++;
                continue;
            }
        }
        String _tmp[] = new String[c];
        for (int i = 0, j = 0; i < tmp.length; i++) {
            if(tmp[i] == null)
                continue;
            _tmp[j++] = tmp[i];
        }
        String str = String.join(" ", _tmp);
        return str;
    }

    //CHECA SE O ARQUIVO JA NAO ESTA CRIADO
    private boolean verificaArquivo(String caminho) {
        try {
            BufferedReader buffReaderIdx = new BufferedReader(new FileReader(caminho));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //CRIA UM MAPA DE CHAVE VALOR DOS NOMES DAS COLUNAS COM
    //O TAMANHO QUE OCUPARÃO
    private void insereTamanhoCabecalho(String c) {
        String[] tmp = c.split(",");

         for (String t: tmp) {
            if(t.equals("ID")) {
                this.tamanhoDados.put("ID", 8);
            }
            if(t.equals("job")) {
                this.tamanhoDados.put("job", 32);
            }
            if(t.equals("e-mail")) {
                this.tamanhoDados.put("e-mail", 40);
            }
            if(t.equals("name")) {
                this.tamanhoDados.put("name", 24);
            }
            if(t.equals("city")) {
                this.tamanhoDados.put("city", 16);
            }
        }
    }

    //METODO PARA COMPLETAR COM ESPACOS EM BRANCO O TOTAL DE
    //BYTES DE DETERMINADA COLUNA DO ARQUIVO
    private char[] colocaEspacoEmBranco(String s, int t) {
        //pega os caracteres separados do nome
        char[] charsTmp = new char[t];
        char[] _chars = s.toCharArray();
        for(int i=0; i < s.length(); i++) {
            charsTmp[i] = _chars[i];
        }
        //insere o espaço em branco no nome
        for(int i=s.length(); i < t; i++) {
            charsTmp[i] = ' ';
        }
        return charsTmp;
    }

    private String converteBinarioTexto (String string) {
        String teste = "";
        String s = prettyBinary(string,8, " ");
        String[] tmp = s.split(" ");
        for (String _tmp: tmp) {
            int charCode = Integer.parseInt(_tmp, 2);
            teste +=  Character.valueOf((char)charCode).toString();
        }
        return teste;
    }
}
