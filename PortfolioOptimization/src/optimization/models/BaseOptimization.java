package optimization.models;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import parameters.Parameters;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public abstract class BaseOptimization {

    protected Portfolio portfolio;
    protected Parameters params;

    protected IloCplex model;
    protected IloNumVar[] w; // positive weights variables for portfolio

    public static final double SMALL = 0.0;
    public static final double BIG = 99.0;
    
    public BaseOptimization(Portfolio portfolio, Parameters params) throws IloException {
        this.portfolio = portfolio;
        this.params = params;

        this.model = new IloCplex();
        this.model.setParam(IloCplex.IntParam.RandomSeed, 1996);
        this.model.setParam(IloCplex.IntParam.TimeLimit, params.getOptimizationTimeLimit());
    }

    public abstract void createDecisionVariables() throws IloException;

    public abstract void setObjectiveFunction() throws IloException;

    public abstract void setConstraints() throws IloException;

    public abstract void printAtEnd() throws IloException;

    public void prepare() throws IloException {
        createDecisionVariables();
        setObjectiveFunction();
        setConstraints();
    }
    
    public boolean solve() throws IloException {
        if (model.solve()) {
            assignWeightsToPortfolio();
            printAtEnd();

            System.out.println("-----------------------------------------");
            System.out.println("Solution status = " + model.getStatus());
            System.out.println("O.F. mean value = " + model.getObjValue());
            System.out.println("-----------------------------------------");
            return true;
        }

        System.out.println("-----------------------------------------");
        System.out.println("Solution status = " + model.getStatus());
        System.out.println("-----------------------------------------");
        return false;
    }
    
    protected void createBasicDecisionVariables() throws IloException {
        w = new IloNumVar[portfolio.getN()];

        for (int i = 0; i < portfolio.getN(); i++) {
            w[i] = model.numVar(0.0, 1.0);
        }
    }
    
    
    protected void setWeightsConstraints() throws IloException {
        IloLinearNumExpr expr = model.linearNumExpr();
        
        for (int i = 0; i < portfolio.getN(); i++) {
            expr.addTerm(1.0, w[i]);
        }
        model.addEq(expr, 1.0);
    }

    private void assignWeightsToPortfolio() throws IloException {
        for (int i = 0; i < portfolio.getN(); i++) {
            if (model.getValue(w[i]) > 0) {
                portfolio.setAssetWeight(i, model.getValue(w[i]));
            } else {
                portfolio.setAssetWeight(i, 0.0);
            }
        }
    }
    
    protected void printPortfoliosWeights() throws IloException {
        System.out.println("-----------------------------------------");
        System.out.println("PORTFOLIO:");
        int count = 0;
        double sum_weights = 0.0;
        for (int i = 0; i < portfolio.getN(); i++) {
            if (model.getValue(w[i]) > 0) {
                System.out.format("%10s -> %5s%%\n", portfolio.getAssetName(i), portfolio.getAssetWeight(i));
                sum_weights += portfolio.getAssetWeight(i);
                count++;
            }
        }
        System.out.println("Total assets: " + count + ";\tTotal weight: " + sum_weights);
    }
    
    public Portfolio getPortfolio() {
        return portfolio;
    }

    public Parameters getParameters() {
        return params;
    }
    
    public IloCplex getModel() {
        return model;
    }
    
    public IloNumVar getW(int i){
        return w[i];
    }
}
