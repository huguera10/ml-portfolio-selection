/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.models.markowitz;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloQuadNumExpr;
import optimization.models.BaseOptimization;
import parameters.Parameters;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public class MarkowitzOptimization extends BaseOptimization {

    public static final double PORTFOLIO_MIN_RETURN = 0.0;

    public MarkowitzOptimization(Portfolio portfolio, Parameters params) throws IloException {
        super(portfolio, params);
        super.prepare();

//        model.setParam(IloCplex.Param.OptimalityTarget, 3);
    }

    @Override
    public void createDecisionVariables() throws IloException {
        createBasicDecisionVariables();
    }

    @Override
    public void setObjectiveFunction() throws IloException {
        IloQuadNumExpr quadFo = model.quadNumExpr();

        double covarianceMatrix[][] = portfolio.getCovarianceMatrix();

        for (int i = 0; i < portfolio.getN(); i++) {
            for (int j = 0; j < portfolio.getN(); j++) {
                quadFo.addTerm(covarianceMatrix[i][j], w[i], w[j]);
            }
        }

        model.addMinimize(model.prod(1.0 / 2.0, quadFo));
    }

    @Override
    public void setConstraints() throws IloException {
        IloLinearNumExpr expr = model.linearNumExpr();
        for (int i = 0; i < portfolio.getN(); i++) {
            expr.addTerm(portfolio.getAssetMeanReturn(i), w[i]);
        }
        model.addGe(expr, PORTFOLIO_MIN_RETURN);

        for (int i = 0; i < portfolio.getN(); i++) {
            model.addLe(w[i], 0.20);
        }

        setWeightsConstraints();
    }

    @Override
    public void printAtEnd() throws IloException {
        printPortfoliosWeights();

        System.out.println("-----------------------------------------");
    }
}
