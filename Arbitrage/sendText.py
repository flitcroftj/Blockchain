import smtplib 
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

def send_message(message_type, percent):
    email = "jflitcroft15@gmail.com"
    pas = "JFlitti2000"

    sms_gateway = '2623749206@vtext.com'
    # The server we use to send emails in our case it will be gmail but every email provider has a different smtp 
    # and port is also provided by the email provider.
    smtp = "smtp.gmail.com" 
    port = 587
    # This will start our email server
    server = smtplib.SMTP(smtp,port)
    # Starting the server
    server.starttls()
    # Now we need to login
    server.login(email,pas)

    # Now we use the MIME module to structure our message.
    msg = MIMEMultipart()
    msg['From'] = email
    msg['To'] = sms_gateway

    # Make sure you add a new line in the subject
    if message_type == 'OOR':
        msg['Subject'] = "Opportunity Gone"
        body = "The window closed"
    else:
        msg['Subject'] = "Arbitrage Opportunity"
        body = "Current difference: " + str(round(percent*100, 3)) + "%% for doing on: " + message_type

    msg.attach(MIMEText(body, 'plain'))

    sms = msg.as_string()

    server.sendmail(email,sms_gateway,sms)

    # lastly quit the server
    server.quit()