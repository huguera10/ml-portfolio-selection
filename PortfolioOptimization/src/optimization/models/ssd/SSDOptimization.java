/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.models;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import java.util.List;
import parameters.Parameters;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public class SSDOptimization extends BaseOptimization {

    private IloNumVar v;

    public SSDOptimization(Portfolio portfolio, Parameters params) throws IloException {
        super(portfolio, params);
    }

    @Override
    public void createDecisionVariables() throws IloException {
        createBasicDecisionVariables();

        v = model.numVar(-BIG, BIG);
    }

    @Override
    public void setObjectiveFunction() throws IloException {
        model.addMaximize(v);
    }

    @Override
    public void setConstraints() throws IloException {       
        setWeightsConstraints();
    }

    @Override
    public void printAtEnd() throws IloException {
        printPortfoliosWeights();

        System.out.println("-----------------------------------------");
    }

    public void setScenariosConstraints(List<int[]> combinations, int nScenariosConstraint, double t) throws IloException {
        IloLinearNumExpr expr;
        double constant = 1.0 / portfolio.getS();
        
        for (int[] combination : combinations) {

            expr = model.linearNumExpr();
            for (int s_index = 0; s_index < nScenariosConstraint; s_index++) {
                int s = combination[s_index];

                for (int i = 0; i < portfolio.getN(); i++) {

                    expr.addTerm(portfolio.getAsset(i).getReturn(s), w[i]);
                }
            }
            model.addLe(v, model.diff(model.prod(constant, expr), t));
        }
    }

    public IloNumVar getV() {
        return v;
    }
}
