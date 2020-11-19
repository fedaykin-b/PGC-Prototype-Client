package com.brunoarruda.hyperdcpabe.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class CommandExecutionData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int taskID;
    private final String label;
    private final List<MethodExecutionData> methodStack;
    private long execTime, gasCost;
    private List<Long> gasPrice;
    private List<Long> gasLimit;
    private double etherCost;
    MethodExecutionData currentMethod;

    @JsonCreator
    public CommandExecutionData (@JsonProperty("taskID") int taskID, @JsonProperty("label") String label, @JsonProperty("methodStack") List<MethodExecutionData> methodStack, @JsonProperty("execTime") long execTime, @JsonProperty("gasCost") long gasCost, @JsonProperty("etherCost") double etherCost, @JsonProperty("gasPrice") List<Long> gasPrice, @JsonProperty("gasLimit") List<Long> gasLimit) {
        this.taskID = taskID;
        this.label = label;
        this.methodStack = methodStack;
        this.execTime = execTime;
        this.gasCost = gasCost;
        this.etherCost =  etherCost;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }

    public CommandExecutionData (int taskID, String label) {
        this.taskID = taskID;
        this.label = label;
        this.methodStack = new ArrayList<MethodExecutionData>(20);
        this.gasPrice = new ArrayList<Long>();
        this.gasLimit = new ArrayList<Long>();
    }

    public void start(String className, String task) {
        currentMethod = new MethodExecutionData(className, task, currentMethod);
        currentMethod.start();
        methodStack.add(currentMethod);
    }

    public void end() {
        currentMethod.end();
        MethodExecutionData parentMethod = currentMethod.getParentMethod();

        if (parentMethod != null) {
            parentMethod.subtractTime(currentMethod.getExecTime());
        } else {
            long last_end = methodStack.get(methodStack.size() - 1).getEnd();
            execTime = last_end - currentMethod.getStart();
        }
        currentMethod = parentMethod;
    }

	public boolean finished() {
		return currentMethod == null;
	}

    public void addGasCost(long gas, long gasPrice) {
        double etherCost = currentMethod.addGasCost(gas, gasPrice);
        this.gasCost += gas;
        this.gasPrice.add(gasPrice);
        this.etherCost += etherCost;
    }
    public void saveGasLimit(long gasLimit) {
        currentMethod.saveGasLimit(gasLimit);
        this.gasLimit.add(gasLimit);
	}

    /*
     * GETTERS
    */
    public int getTaskID() {
        return taskID;
    }

    public String getLabel() {
        return label;
    }

    public List<MethodExecutionData> getMethodStack() {
        return methodStack;
    }

    public long getExecTime() {
        return execTime;
    }

    public long getGasCost() {
        return gasCost;
    }

    public double getEtherCost() {
        return etherCost;
    }

    public List<Long> getGasPrice() {
        return gasPrice;
    }

    public List<Long> getGasLimit() {
        return gasLimit;
    }

    @Override
    public String toString() {
        String base_str = "{\n#: %d, task: %s, execTime: %d, gasCost: %d, methodCalling: [\n\t%s\n\t]\n}";
        List<String> methodCalling_str = new ArrayList<String>(methodStack.size());
        methodStack.forEach((m) -> methodCalling_str.add(m.toString()));
        return String.format(base_str, taskID, label, execTime, gasCost, String.join(",\n\t", methodCalling_str));
    }
}
