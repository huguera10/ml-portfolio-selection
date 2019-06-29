#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sun Feb 10 17:24:25 2019

@author: hugo
"""

import sys
import json
import numpy as np
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
sns.set()

# %%


def readDataFrame(path):
    df = pd.read_csv(path)
    return df


def sliceDataFrameByDate(df, start_date, end_date):
    df = df[df["date"].map(str) >= start_date]
    df = df[df["date"].map(str) <= end_date]
    return df


def selectDataFrameAssets(df, portfolio_weights):
    df_selected = df[["date"]]
    assets_portfolio_plus = list(portfolio_weights["PORTFOLIO"].keys())
    df_selected = pd.concat([df_selected, df[assets_portfolio_plus]], axis=1)

    assets_portfolio_minus = list(portfolio_weights["TRACKED_INDEX"].keys())
    df_selected = pd.concat([df_selected, df[assets_portfolio_minus]], axis=1)

    return df_selected


def parsePricesToReturns(df, portfolio_weights):
    for asset in portfolio_weights["PORTFOLIO"].keys():
        df[asset] = np.log(df[asset]) - np.log(df[asset].shift())

    for asset in portfolio_weights["TRACKED_INDEX"].keys():
        df[asset] = np.log(df[asset]) - np.log(df[asset].shift())

    return df


def buildPortfolios(df, portfolio_weights):
    df["PORTFOLIO"] = [0.0] * len(df)
    for asset, weight in portfolio_weights["PORTFOLIO"].items():
        df["PORTFOLIO"] = df["PORTFOLIO"] + df[asset] * float(weight)

    df["TRACKED_INDEX"] = [0.0] * len(df)
    for asset, weight in portfolio_weights["TRACKED_INDEX"].items():
        df["TRACKED_INDEX"] = df["TRACKED_INDEX"] + df[asset] * float(weight)

    return df.dropna()


def plotPortfolios(portfolio, tracked_index):
    plt.figure(figsize=(8, 4))
    plt.plot(range(len(df)), portfolio, label="PORTFOLIO")
    plt.plot(range(len(df)), tracked_index, label="TRACKED_INDEX")
    plt.legend()
    plt.show()


def plotPortfoliosCDF(portfolio, tracked_index):
    plt.figure(figsize=(8, 4))
    plt.hist(
        portfolio,
        bins=250,
        cumulative=True,
        histtype="step",
        label="CDF PORTFOLIO"
    )
    plt.hist(
        tracked_index,
        bins=250,
        cumulative=True,
        histtype="step",
        label="CDF TRACKED_INDEX"
    )
    plt.legend()
    plt.show()


def plotPortfoliosDiff(portfolio, tracked_index):
    plt.figure(figsize=(8, 4))
    plt.plot(range(len(df)), portfolio -
             tracked_index, label="PORTFOLIOS_DIFF")
    plt.ylabel("diff between portfolio and tracked_index")
    plt.xlabel("Data points")
    plt.legend()
    plt.show()

# %%


if __name__ == "__main__":
    portfolio_weights_file = sys.argv[1]

    with open(portfolio_weights_file) as file:
        portfolio_weights = json.load(file)

    df = readDataFrame(portfolio_weights["DATA_PATH"])
    df = sliceDataFrameByDate(
        df, portfolio_weights["START_DATE"], portfolio_weights["END_DATE"])
    df = selectDataFrameAssets(df, portfolio_weights)

    df = parsePricesToReturns(df, portfolio_weights)
    df = buildPortfolios(df, portfolio_weights)

    plotPortfolios(df["PORTFOLIO"].cumsum(), df["TRACKED_INDEX"].cumsum())
    plotPortfoliosCDF(df["PORTFOLIO"].sort_values(),
                      df["TRACKED_INDEX"].sort_values())
    plotPortfoliosDiff(df["PORTFOLIO"].cumsum(), df["TRACKED_INDEX"].cumsum())
