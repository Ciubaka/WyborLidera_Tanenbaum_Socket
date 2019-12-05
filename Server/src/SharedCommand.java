public class SharedCommand {

    private String zmienna = null;
    private boolean czyNowy = false;

    public boolean isCzyNowy() {
        boolean tmp = czyNowy;
        synchronized (this) {
            czyNowy=false;
        }
        return tmp;

    }

    public void setCzyNowy(boolean czyNowy) {
        synchronized (this) {
            this.czyNowy = czyNowy;
        }
    }





    private boolean clientExist;

    public SharedCommand() {
        clientExist = true;
    }

    public boolean isClientExist() {
        return clientExist;
    }

    public void setClientExist(boolean clientExist) {
        this.clientExist = clientExist;
    }


    public void set(String string){
        synchronized (this){
            zmienna = string;
        }

    }

    public String take(){
        String zm = zmienna;
        synchronized (this){
            zmienna = null;
        };
        return zm;
    }

    public boolean ready(){
         return zmienna != null;

    }
}
