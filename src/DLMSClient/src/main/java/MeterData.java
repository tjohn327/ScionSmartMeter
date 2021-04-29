public class MeterData {


    private float frequency;
    private float voltage;
    private float current;
    private float activePower;
    private float reactivePower;
    private float apparentPower;
    private float powerFactor;
    private float activeEnergy;
    private float reactiveEnergy;

    public MeterData(){

    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    public float getActivePower() {
        return activePower;
    }

    public void setActivePower(float activePower) {
        this.activePower = activePower;
    }

    public float getReactivePower() {
        return reactivePower;
    }

    public void setReactivePower(float reactivePower) {
        this.reactivePower = reactivePower;
    }

    public float getApparentPower() {
        return apparentPower;
    }

    public void setApparentPower(float apparentPower) {
        this.apparentPower = apparentPower;
    }

    public float getPowerFactor() {
        return powerFactor;
    }

    public void setPowerFactor(float powerFactor) {
        this.powerFactor = powerFactor;
    }

    public float getActiveEnergy() {
        return activeEnergy;
    }

    public void setActiveEnergy(float activeEnergy) {
        this.activeEnergy = activeEnergy;
    }

    public float getReactiveEnergy() {
        return reactiveEnergy;
    }

    public void setReactiveEnergy(float reactiveEnergy) {
        this.reactiveEnergy = reactiveEnergy;
    }
    public void print() {
        System.out.printf("Voltage \t: %.2f V\n", voltage);
        System.out.printf("Current \t: %.2f A\n", current);
        System.out.printf("Frequency \t: %.2f Hz\n", frequency);
        System.out.printf("Active Power \t: %.3f kW\n", activePower);
        System.out.printf("Reactive Power \t: %.3f kvar\n", reactivePower);
        System.out.printf("Apparent Power \t: %.3f kVA\n", apparentPower);
        System.out.printf("Power Factor \t: %.3f\n", powerFactor);
        System.out.printf("Active Energy \t: %.2f kWh\n", activeEnergy);
        System.out.printf("Reactive Energy\t: %.2f kvarh\n", reactiveEnergy);
    }

    public String toString(){
        String string = String.format("Voltage \t: %.2f V\nCurrent \t: %.2f A\nFrequency \t: %.2f Hz\n" +
        "Active Power \t: %.3f kW\n"+
        "Reactive Power \t: %.3f kvar\n"+
        "Apparent Power \t: %.3f kVA\n"+
        "Power Factor \t: %.3f\n"+
        "Active Energy \t: %.2f kWh\n"+
        "Reactive Energy\t: %.2f kvarh\n",
         voltage,current,frequency,activePower,reactivePower,apparentPower,powerFactor,
                activeEnergy,reactiveEnergy);
        return string;
    }

    public String toJsonString(){
        String string = String.format("{ \n\"Voltage\" : %.2f,\n"+
                        "\"Current\": %.2f, \n\"Frequency\" : %.2f,\n" +
                        "\"ActivePower\" : %.3f,\n"+
                        "\"ReactivePower\" : %.3f,\n"+
                        "\"ApparentPower\" : %.3f,\n"+
                        "\"PowerFactor\" : %.3f,\n"+
                        "\"ActiveEnergy\" : %.2f,\n"+
                        "\"ReactiveEnergy\" : %.2f\n}",
                voltage,current,frequency,activePower,reactivePower,apparentPower,powerFactor,
                activeEnergy,reactiveEnergy);
        return string;
    }
}
