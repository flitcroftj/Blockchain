import pandas as pd
from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.chrome.options import Options
import time
import csv
from sendText import send_message


def run_check(is_test, swap, binance):
    options = Options()
    options.headless = True
    driver1 = webdriver.Chrome(options=options)
    driver2 = webdriver.Chrome(options=options)

    if not is_test:
        driver1.get("https://secretanalytics.xyz/secret14zv2fdsfwqzxqt7s2ushp4c4jr56ysyld5zcdf")
        driver2.get("https://www.tradingview.com/symbols/SCRTETH/?exchange=BINANCE")
        time.sleep(10)

    #Used to keep track of if we switch in or out of a range
    within_arbitrage_range = None
    #Used for setting if in range initially
    first_run = True

    while(1==1):
        try:
            swap_ratio = None
            binance_ratio = None

            if not is_test:
                swap_ratio, binance_ratio = get_data(driver1, driver2)
            else:
                swap_ratio = swap
                binance_ratio = binance

            diff = swap_ratio - binance_ratio
            print(diff)

            if first_run:
                if abs(diff/binance_ratio) > 0.05 and diff < 0 or diff/swap_ratio > 0.1 and diff > 0:
                    within_arbitrage_range = True
                else: 
                    within_arbitrage_range = False
                first_run = False

            test_result, within_arbitrage_range = send_emails_from_diff(diff, within_arbitrage_range, swap_ratio, binance_ratio)

            if is_test:
                return test_result

        except Exception as e:
            print("Catching: " + str(e))

    driver1.close()
    driver2.close()


def get_data(driver1, driver2):
    #Reload pages for most recent data
    driver1.refresh()
    driver2.refresh()
    time.sleep(10)
    delay = 10

    #Get sscrt and seth pools from the swap and calculate ratio
    try:
        sscrt_elem = WebDriverWait(driver1, delay).until(EC.presence_of_element_located((By.XPATH, "//h6[contains(text(), 'SSCRT')]")))
        seth_elem = WebDriverWait(driver1, delay).until(EC.presence_of_element_located((By.XPATH, "//h6[contains(text(), 'SETH')]")))
    except TimeoutException:
        print("Failed in swap")
    sscrt = float(sscrt_elem.text.split(' ')[0].replace(',', ''))
    seth = float(seth_elem.text.split(' ')[0].replace(',', ''))
    swap_ratio = seth/sscrt

    #Get scrt/eth binance pair from tradingview
    try:
        trading_view = WebDriverWait(driver2, delay).until(EC.presence_of_element_located((By.XPATH, "//*[@id='anchor-page-1']/div/div[3]/div[1]/div/div/div/div[1]/div[1]")))
    except TimeoutException:
        print("Failed in tradingview")

    binance_ratio = float(trading_view.text)

    with open('ratioData.csv', 'a', newline='') as file:
        writer = csv.writer(file, delimiter=' ') 
        writer.writerow([str(binance_ratio) + ',' + str(swap_ratio)])
        file.close()

    print("Binance: " + str(round(binance_ratio, 6)))
    print("Swap: " + str(round(swap_ratio, 6)))

    return swap_ratio, binance_ratio


def send_emails_from_diff(diff, within_arbitrage_range, swap_ratio, binance_ratio):
    test_result = ""
    if diff < 0:
        diff = abs(diff)
        test_result = "Governence"
        if diff/binance_ratio > 0.05:
            test_result += " going in"
            if within_arbitrage_range == False:
                send_gov_email(diff/binance_ratio)
                within_arbitrage_range = True
        else:
            test_result += " going out"
            if within_arbitrage_range == True:
                send_OOR_email()
                within_arbitrage_range = False
    else: 
        test_result = "Arbitrage"
        if diff/swap_ratio > 0.1:
            test_result += " going in"
            if within_arbitrage_range == False:
                send_arbitrage_email(diff/swap_ratio)
                within_arbitrage_range = True
        else:
            test_result += " going out"
            if within_arbitrage_range == True:
                send_OOR_email()
                within_arbitrage_range = False
    return test_result, within_arbitrage_range


def send_gov_email(percent):
    print("Sending governence text")
    send_message("governence.", percent)

def send_arbitrage_email(percent):
    print("Sending arbitrage text")
    send_message("governence.", percent)

def send_OOR_email():
    send_message("OOR", None)

def main():
    run_check(False, None, None)

if __name__ == "__main__":
    """ This is executed when run from the command line """
    main()