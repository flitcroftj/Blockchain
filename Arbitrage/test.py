import csv

with open('ratioData.csv', 'a', newline='') as file:
    writer = csv.writer(file, delimiter=' ') 
    writer.writerow(['binance', 'swap'])
    file.close()
 