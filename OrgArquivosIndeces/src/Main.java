import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static FileHandler fh = new FileHandler();

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        fh.criaArquivoBinario();

        try {
            fh.criaHash();
        } catch (Exception e) {
            System.out.println(e);
        }

        //Dados dadoHash = fh.buscaHash(9);
        //System.out.println(dadoHash.toString());

        try {
            //fh.criaArvore();
        } catch (Exception e) {
            System.out.println(e);
        }
        /*INDICES EM ARQUIVO - CHAVE 1 - NOME*/
        criaArquivo("ind", "dados_totais.csv", "name_indexado.csv");
        //esse arquivo aux para montar a tabela secundadria
        criaArquivo("bin", "name_indexado_aux.csv", "binario_indexado_name_aux.txt");

        System.out.println("**********************************************");
        try {
            System.out.println("BUSCAR DADO POR NOME");
            System.out.println("nome : Enoch Price");

            int i = fh.buscaBinariaIDPorNome("Enoch Price");
            fh.buscaLinhaBinariaPorId(i);
            Dados d = fh.getDados();
            System.out.println(d.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("**********************************************");
        /*INDICES EM ARQUIVO - CHAVE 3 - EMAIL*/
        criaArquivo("ind", "dados_totais.csv", "e-mail_indexado.csv");

        criaArquivo("bin", "e-mail_indexado_aux.csv", "binario_indexado_e-mail_aux.txt");

        try {
            int i = fh.buscaBinariaIDPorEmail("Kassandra_Webster9777@hourpy.biz");
            fh.buscaLinhaBinariaPorId(i);
            Dados d = fh.getDados();
            System.out.println(d.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void criaArquivo(String tipo, String caminhoOriginal, String caminhoFinal) {

        if(tipo.equals("ind")) {
            try {
                fh.criaArquivoIndexado(caminhoOriginal, caminhoFinal);
            } catch (Exception e) {
                System.out.println(e);
            }
        } else if(tipo.equals("bin")) {
            try {
                fh.criaArquivoBinario(caminhoOriginal, caminhoFinal);
            } catch (Exception e) {
                System.out.println(e);
            }
        }


    }
}
