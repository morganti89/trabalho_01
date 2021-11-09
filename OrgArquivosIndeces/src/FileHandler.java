import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHandler {

    private File arquivo_original = null;
    private final int QTD_LINHAS = 1000;
    private final String caminho_raiz = "C:\\Users\\junior\\Desktop\\faculdade\\semestre 7\\algorimo e estrutura de dados 2\\trabalho\\arquivo\\";
    private final String caminho_original = "C:\\Users\\junior\\Desktop\\faculdade\\semestre 7\\algorimo e estrutura de dados 2\\trabalho\\arquivo\\dados_totais.csv";

    private String cabecalho;
    private Dados dados;
    ArrayList<Dados> lista_dados = new ArrayList<Dados>();
    private HashMap <String, Integer> tamanho_dados = new HashMap();
    private HashMap <Integer, Dados> hashMap = new HashMap<>();
    private HashMap<String, Integer> tabelaAuxEnd;
    private BTree tree = null;

    public Dados getDados() {
        return dados;
    }

    public HashMap getHashMap() {
        return hashMap;
    }

    public Dados buscaHash(int i) {
        return this.hashMap.get(i);
    }

    public void criaHash() throws FileNotFoundException {
        BufferedReader buffReader = new BufferedReader(new FileReader(caminho_original));
        String linha = "";

        if(this.hashMap.size() > 0)
            return;

        try {
            //le o arquivo
            while((linha = buffReader.readLine()) != null) {
                if(linha.equals("ID,job,e-mail,name,city")){
                    continue;
                }
                String[] tmp = linha.split(",");
                dados = new Dados(tmp);
                this.hashMap.put(dados.getHash(), dados);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void criaArvore() throws FileNotFoundException {
        BufferedReader buffReader = new BufferedReader(new FileReader(caminho_original));
        String linha = "";

        if(this.tree != null)
            return;

        try {
            //le o arquivo
            while((linha = buffReader.readLine()) != null) {
                if(linha.equals("ID,job,e-mail,name,city")){
                    continue;
                }
                String[] tmp = linha.split(",");
                dados = new Dados(tmp);
                this.tree = this.tree.insert(dados, this.tree);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        this.tree.print(this.tree);
        System.out.println();
    }

    public FileHandler(){
        this.arquivo_original = new File(this.caminho_original);
    }

    private void setCabecalho(String caminho) throws IOException {
        BufferedReader buffReader = new BufferedReader(new FileReader(caminho));
        String rowReader = "";
        rowReader = buffReader.readLine();
        insereTamanhoCabecalho(rowReader);
        this.cabecalho = rowReader;
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
            if(lista_dados.size() == 0) {
                while((linha = buffReader.readLine()) != null) {
                    if(linha.equals("ID,job,e-mail,name,city")){
                        continue;
                    }
                    String[] tmp = linha.split(",");
                    dados = new Dados(tmp);
                    dados.setChave(chave); //???
                    lista_dados.add(dados);
                }
                //organiza o arquivo
                lista_dados.sort((a,b) -> (a.compareTo(b)));
            }
            this.tabelaAuxEnd = new HashMap<>();
            //escreve em um novo arquivo
            FileWriter fw = new FileWriter(caminhoAbsoluto, true);
            String caminhoAbsolutoAux = this.caminho_raiz +chave+  "_indexado_aux.csv";
            FileWriter fwa = new FileWriter(caminhoAbsolutoAux, true);

            int i = 0;
            int k = 0;
            String dadoCsv = "";
            if(i == 0){
                dadoCsv = "end,"+chave+",ID";
                fw.append(dadoCsv);
                fw.append("\n");

                dadoCsv = chave+",end";
                fwa.append(dadoCsv);
                fwa.append("\n");
                k++;
                i++;
            }
            for (Dados _d: lista_dados) {
                //arquivo secundario
                if(i % QTD_LINHAS== 0) {
                    String _s = Integer.toString(k);

                    if(chave.equals("name")) {
                        this.tabelaAuxEnd.put(_d.getName(), k);
                        dadoCsv = _d.getName()+","+_s;
                    } else if (chave.equals("e-mail")) {
                        this.tabelaAuxEnd.put(_d.getEmail(), k);
                        dadoCsv = _d.getEmail()+","+_s;
                    }
                    fwa.append(dadoCsv);
                    fwa.append("\n");
                    k = i + 1;
                }

                //arquivo principal
                String s = Integer.toString(i);
                if(chave.equals("name")) {
                    dadoCsv = s+","+_d.getName()+","+_d.getId();
                } else if (chave.equals("e-mail")) {
                    dadoCsv = s+","+_d.getEmail()+","+_d.getId();
                }
                fw.append(dadoCsv);
                fw.append("\n");
                i++;
            }
            fw.flush();
            fw.close();
            fwa.flush();
            fwa.close();

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
        String caminho = this.caminho_raiz+arquivoFinal;

        //grava no arquivo binario
        BufferedWriter buffWriter = new BufferedWriter(new FileWriter(caminho));
        int c = 0;
        int i = 0;
        boolean criarArquivo = true;
        while (true) {
            rowReader = buffReader.readLine();
            int tamanhoCabecalho = 0;
            if (rowReader != null) {
                if(c == 0) {
                    insereTamanhoCabecalho(rowReader);
                    this.cabecalho = rowReader;
                    c++;
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
                i++;
            } else
                break;
        }
        buffReader.close();
        buffWriter.close();
    }

    public void criaArquivoBinario(String arquivoOriginal, String arquivoFinal, int inicio) throws IOException {
        String _arquivoOriginal = this.caminho_raiz+arquivoOriginal;
        //le do arquivo indexado
        BufferedReader buffReader = new BufferedReader(new FileReader(_arquivoOriginal));
        String rowReader = "";
        String caminho = this.caminho_raiz+arquivoFinal;

        //grava no arquivo binario
        BufferedWriter buffWriter = new BufferedWriter(new FileWriter(caminho));
        int fim = QTD_LINHAS + inicio;
        int c = 0;
        while (c <= fim) {
            rowReader = buffReader.readLine();
            int tamanhoCabecalho = 0;
            if (rowReader != null) {
                if(c == 0) {
                    insereTamanhoCabecalho(rowReader);
                    this.cabecalho = rowReader;
                    c++;
                    continue;
                }
                if(c >= inicio && c < fim) {
                    String s = "";
                    try {
                        s = normalizaTextoParaBinario(rowReader, tamanhoCabecalho);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    if(rowReader != "")
                        buffWriter.write(s);
                }

            } else
                break;
            c++;
        }
        buffReader.close();
        buffWriter.close();
    }

    public void criaArquivoBinario() throws IOException {
        BufferedReader buffReader = new BufferedReader(new FileReader(this.caminho_original));

        String rowReader = "";

        //grava no arquivo binario
        String caminho = this.caminho_raiz + "_binario_dados_1.txt";
        BufferedWriter buffWriter1 = new BufferedWriter(new FileWriter(caminho));
        caminho = this.caminho_raiz + "_binario_dados_2.txt";
        BufferedWriter buffWriter2 = new BufferedWriter(new FileWriter(caminho));
        caminho = this.caminho_raiz + "_binario_dados_3.txt";
        BufferedWriter buffWriter3 = new BufferedWriter(new FileWriter(caminho));
        caminho = this.caminho_raiz + "_binario_dados_4.txt";
        BufferedWriter buffWriter4 = new BufferedWriter(new FileWriter(caminho));
        caminho = this.caminho_raiz + "_binario_dados_5.txt";
        BufferedWriter buffWriter5 = new BufferedWriter(new FileWriter(caminho));
        caminho = this.caminho_raiz + "_binario_dados_6.txt";
        BufferedWriter buffWriter6 = new BufferedWriter(new FileWriter(caminho));
        int c = 0, i = 1;
        while (true) {
            rowReader = buffReader.readLine();
            int tamanhoCabecalho = 0;
            if (rowReader != null) {
                if(c == 0) {
                    insereTamanhoCabecalho(rowReader);
                    this.cabecalho = rowReader;
                    c++;
                    continue;
                }
                String s = "";
                try {
                    s = normalizaTextoParaBinario(rowReader, tamanhoCabecalho);
                } catch (Exception e) {
                    System.out.println(e);
                }
                if(rowReader != "") {
                    if(i%7==0){
                        buffWriter1.write(s);
                    } else if(i%6==0) {
                        buffWriter2.write(s);
                    } else if(i%5==0) {
                        buffWriter3.write(s);
                    }  else if(i%4==0) {
                        buffWriter4.write(s);
                    } else if(i%3==0) {
                        buffWriter5.write(s);
                    }else {
                        buffWriter6.write(s);
                    }
                    i++;
                }
            } else
                break;
            c++;
        }
        buffReader.close();
        buffWriter1.close();
        buffWriter2.close();
        buffWriter3.close();
        buffWriter4.close();
        buffWriter5.close();
        buffWriter6.close();
    }

    public int buscaBinariaIDPorNome(String procura) throws IOException {

        Map<String, Integer> hm1 = sortByValue(this.tabelaAuxEnd);
        int endereco = 0;
        for (Map.Entry<String, Integer> en : hm1.entrySet()) {
            if(procura.compareTo(en.getKey()) <= 0) {
                endereco = en.getValue();
                break;
            }
        }
        criaArquivoBinario("name_indexado.csv", "binario_indexado_name.txt", endereco);

        String pr = removeEspacosEmBrancoFinal(converteTextoBinario(procura));
        String resposta = buscaBinaria(pr, "name", this.caminho_raiz + "binario_indexado_name.txt");
        String texto = converteBinarioTexto(resposta);
        return Integer.parseInt(removeEspacosEmBrancoFinal(texto));
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public int buscaBinariaIDPorEmail(String procura) throws IOException {

        Map<String, Integer> hm1 = sortByValue(this.tabelaAuxEnd);
        int endereco = 0;
        for (Map.Entry<String, Integer> en : hm1.entrySet()) {
            if(procura.compareTo(en.getKey()) <= 0) {
                endereco = en.getValue();
                break;
            }
        }
        criaArquivoBinario("e-mail_indexado.csv", "binario_indexado_e-mail.txt", endereco);
        String pr = removeEspacosEmBrancoFinal(converteTextoBinario(procura));
        String resposta = buscaBinaria(pr, "e-mail", this.caminho_raiz + "binario_indexado_e-mail.txt");
        String texto = converteBinarioTexto(resposta);
        return Integer.parseInt(removeEspacosEmBrancoFinal(texto));
    }

    public String[] buscaLinhaBinariaPorId(int procura)  throws IOException {

        BufferedReader buffReader = new BufferedReader(new FileReader(this.caminho_original));
        String rowReader = "";
        rowReader = buffReader.readLine();
        insereTamanhoCabecalho(rowReader);
        this.cabecalho = rowReader;
        buffReader.close();


        String[] cabecalho = this.cabecalho.split(",");
        String[] resposta = new String[cabecalho.length];

        for (int j = 1; j <= 6; j++) {

            String p = Integer.toString(procura);
            String pr = removeEspacosEmBrancoFinal(converteTextoBinario(p));
            String busca = buscaBinaria(pr, "ID", this.caminho_raiz + "_binario_dados_"+j+".txt");

            if(busca.equals("")) continue;

            int tamanhoIn = 0;
            int i = 0;
            for (String cab: cabecalho) {
                int tamanhoFim = this.tamanho_dados.get(cab) + tamanhoIn;
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
            break;
        }
        dados = new Dados(resposta);
        return resposta;
    }

    //RETORNA O DADO EM BINARIO
    private String buscaBinaria(String pr, String tipo, String caminho) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(caminho));
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
            tamanho += this.tamanho_dados.get(cab);
            c++;
        }
        tamanho += c;

        //calcula o salto pelo indice do cabecalho (hashmap)
        int _tipo = this.tamanho_dados.get(tipo);

        RandomAccessFile raf = new RandomAccessFile(caminho, "r");
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

                c = 0;
                //se for a busca em tabela com endereco
                if(cabecalho[0].equals("end")) {
                    c = (this.tamanho_dados.get("end") + 1)*8 ;
                    raf.skipBytes(c);
                }

                for(int i = 0; i < _tipo*8; i++) {
                    int h = raf.read();
                    str +=  Character.valueOf((char)h).toString();
                }
                String st = removeEspacosEmBrancoFinal(str);

                if(fim - meio == 2000) {
                    return "";
                }

                if(pr.compareTo(st) == 0) {
                    String resposta = "";
                    raf.seek(meio);
                    String linha = "";
                    for(int k = 0; k < tamanho*8; k++) {
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
                        idx = linha.substring((this.tamanho_dados.get(tipo) + 1 )*8 + c, tamanho * 8);
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
            char[] c= this.colocaEspacoEmBranco(tmp[i], this.tamanho_dados.get(cabecalho[i]));
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
                this.tamanho_dados.put("ID", 8);
            }
            if(t.equals("job")) {
                this.tamanho_dados.put("job", 32);
            }
            if(t.equals("e-mail")) {
                this.tamanho_dados.put("e-mail", 40);
            }
            if(t.equals("name")) {
                this.tamanho_dados.put("name", 24);
            }
            if(t.equals("city")) {
                this.tamanho_dados.put("city", 16);
            }
             if(t.equals("end")) {
                 this.tamanho_dados.put("end", 8);
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
