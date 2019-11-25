public class SharedCommand {

    private String zmienna = null;



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
