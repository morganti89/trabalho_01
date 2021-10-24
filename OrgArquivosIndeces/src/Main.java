import java.util.Scanner;

public class Main {

    private static FileHandler fh = new FileHandler();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);


        /*INDICES EM ARQUIVO - CHAVE 1 - NOME*/
        System.out.println("--------------------------------------------");
        System.out.println("CRIANDO ARQUIVO INDEXADO PARA NOME");
        System.out.println("--------------------------------------------");
        //criaArquivo("ind", "dados_totais.csv", "e-mail_Indexado.csv");
        criaArquivo("ind", "mock.csv", "name_indexado.csv");
        System.out.println("CRIANDO ARQUIVO BINARIO A PARTIR DO INDEXADO");
        System.out.println("--------------------------------------------");
        criaArquivo("bin", "name_indexado.csv", "binario_indexado_name.txt");
        System.out.println("**********************************************");
        try {
            System.out.println("BUSCAR DADO POR NOME");
            System.out.println("nome : Allison Rossi");
            int i = fh.buscaBinariaIDPorNome("Allison Rossi");
            //criaArquivo("bin", "dados_totais.csv", "binario_dados_totais.txt");
            criaArquivo("bin", "mock.csv", "binario_mock.txt");

            fh.buscaLinhaBinariaPorId(i);
            Dados d = fh.getDados();
            System.out.println(d.toString());
        } catch (Exception e) {
            System.out.println(e);
        }

        /*INDICES EM ARQUIVO - CHAVE 3 - EMAIL*/
        System.out.println("--------------------------------------------");
        System.out.println("CRIANDO ARQUIVO INDEXADO PARA EMAIl");
        System.out.println("--------------------------------------------");
        //criaArquivo("ind", "dados_totais.csv", "e-mail_Indexado.csv");
        criaArquivo("ind", "mock.csv", "e-mail_Indexado.csv");

        System.out.println("CRIANDO ARQUIVO BINARIO A PARTIR DO INDEXADO");
        System.out.println("--------------------------------------------");
        criaArquivo("bin", "e-mail_Indexado.csv", "binario_indexado_email.txt");
        System.out.println("**********************************************");
        try {
            System.out.println("BUSCAR DADO POR EMAIL");
            System.out.println("nome : Luke_Harris9078@eirey.tech");
            int i = fh.buscaBinariaIDPorEmail("Luke_Harris9078@eirey.tech");
            //criaArquivo("bin", "dados_totais.csv", "binario_dados_totais.txt");
            criaArquivo("bin", "mock.csv", "binario_mock.txt");

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
