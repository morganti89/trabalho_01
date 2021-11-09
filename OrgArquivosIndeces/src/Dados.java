public class Dados implements Comparable<Dados>{

    public String id;
    public String name;
    public String job;
    public String email;
    public String city;
    private String chave;
    private int hash;


    public Dados (String[] params) {
        this.id = params[0];
        this.job = params[1];
        this.email = params[2];
        this.name = params[3];
        this.city = params[4];
        calculaHash();
    }
    @Override
    public int compareTo(Dados other) {
        if(chave.equals("e-mail")) {
            if(this.equals(other)) {
                return 0;
            } else if(email.compareToIgnoreCase(other.email) > 0) {
                return 1;
            } else {
                return -1;
            }
        } else if(chave.equals("name")) {
            if(this.equals(other)) {
                return 0;
            } else if(name.compareToIgnoreCase(other.name) > 0) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    public void calculaHash() {
        int tamanho_nome_emprego = (this.name.length() + this.job.length())/2;
        this.hash = tamanho_nome_emprego * Integer.parseInt(this.id);
    }

    public int getHash(){
        return this.hash;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getJob() {
        return job;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Dados{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", job='" + job + '\'' +
                ", email='" + email + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
