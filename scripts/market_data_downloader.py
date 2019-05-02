
import pandas as pd
from argparse import ArgumentParser
from pandas_datareader.data import DataReader


class MarketDataDownloader(object):

    DATE_FORMAT = "%Y-%m-%d"
    DATE_FORMAT_NO_PUNCT = "%Y%m%d"

    def __init__(
        self,
        initial_date,
        final_date,
        tickers_list=None,
        tickers_file_path=None,
        data_source="yahoo",
        output_file_path="output.csv",
        return_attributes=None
    ):
        self.final_date = final_date
        self.initial_date = initial_date
        self.output_file_path = output_file_path
        self.data_source = data_source
        self.return_attributes = return_attributes

        if tickers_list is None:
            self.tickers_list = self.read_tickers_from_file(tickers_file_path)
        else:
            self.tickers_list = tickers_list

    def read_tickers_from_file(self, tickers_file_path):
        with open(tickers_file_path, "r") as tickers_file:
            tickers_string = tickers_file.readline()

        tickers_list = [ticker for ticker in tickers_string.split(",")]

        return tickers_list

    def get_dates(self):
        dates_list = \
            pd.date_range(self.initial_date, self.final_date, freq="D")

        return [date.date() for date in dates_list]

    def download_market_data(self):
        fail_count = 0
        success_count = 0
        df = pd.DataFrame(index=self.get_dates())

        for ticker in self.tickers_list:
            try:
                result = DataReader(
                    ticker,
                    self.data_source,
                    self.initial_date,
                    self.final_date
                )
                print("Got data for {} from {}. Shape: {}".format(
                    ticker, self.data_source, result.shape
                ))
                success_count += 1

                if self.return_attributes is None:
                    df[ticker] = result["Adj Open"]
                else:
                    df[["{}_{}".format(ticker, attribue)
                        for attribue in self.return_attributes]] = \
                        result[self.return_attributes]
            except:
                print("Did not get data for {}".format(ticker))
                fail_count += 1

        print("Succed in {}. Failed in {}".format(success_count, fail_count))

        return df

    def store_market_data(self, df):
        df.to_csv(self.output_file_path, index=True)

    def run(self):
        df = self.download_market_data()
        self.store_market_data(df)


if __name__ == "__main__":
    parser = ArgumentParser()

    parser.add_argument(
        "--initial-date", type=str, required=True,
        help="Initial date to download data. Format: \"YYYY-MM-DD\"."
    )
    parser.add_argument(
        "--final-date", type=str, required=True,
        help="Final date to download data. Format: \"YYYY-MM-DD\"."
    )
    parser.add_argument(
        "--tickers-list", type=str, required=False, default=None,
        help="Tickers to download. Format: \"ticker1,ticker2, ticker3\"."
    )
    parser.add_argument(
        "--tickers-file-path", type=str, required=False,
        help="Path for file with tickers to download. Use this argument if "
        "\"--tickers-list\" is not being used."
        "File Format: \"ticker1,ticker2, ticker3\"."
    )
    parser.add_argument(
        "--data-source", type=str, required=False, default="yahoo",
        help="Source to download data. Ex: yahoo, google, fred, ff."
    )
    parser.add_argument(
        "--output-file-path", type=str, required=False, default="output.csv",
        help="Output file path to store market data."
    )
    parser.add_argument(
        "--return-attributes", type=str, required=False, default=None,
        help="Data attributes to store."
    )

    args = parser.parse_args()

    market_data_downloader = MarketDataDownloader(
        args.initial_date,
        args.final_date,
        None if args.tickers_list is None
        else args.tickers_list.replace(" ", "").split(","),
        args.tickers_file_path,
        args.data_source,
        args.output_file_path,
        args.return_attributes
    )
    market_data_downloader.run()
