import urllib.request,re

def display_list(list_tr):
    for tr in list_tr:
        print(tr)
        print('****************************')

def read_web_page(year, file):
    #file = open('ICC_Rankings_'+year,'w')
    try:
        web_page = urllib.request.urlopen("https://en.wikipedia.org/wiki/International_cricket_in_"+year)
        lines = web_page.read().decode(errors="replace")
        web_page.close()

        list_lines = lines.split('<a href="/wiki/ICC_ODI_Championship" title="ICC ODI Championship">ICC ODI Championship')

        if(year in ('2011','2012')):
            list_lines = list_lines[1:]
        
        list_tr = list_lines[1].split("</table>")[0].split("<tr>")
        main_list_td = []

        #display_list(list_tr)
        for i in range(1,len(list_tr)):
            tr = list_tr[i].split("</tr>")
            for temp_row in tr:
                temp_list_td = re.findall('<td>[^`]*',temp_row)
                if len(temp_list_td) > 0:
                    main_list_td.append(temp_list_td[0].split('\n'))
                
        for list_td in main_list_td:
            for i in range(2):
                if(i==0):
                  file.write(list_td[i].split('</td>')[0].split("<td>")[1]+",")  
                  print(list_td[i].split('</td>')[0].split("<td>")[1],end=',')
                else:
                  print(list_td[i].split("<a href=")[1].split('">')[1].split("</a>")[0])
                  file.write(list_td[i].split("<a href=")[1].split('">')[1].split("</a>")[0]+"\n")  
                  #print(list_td[i].split("<a href=")[0].split('">')[1].split("</a>")[0])

        print()
        #file.close()
    except:
        print('Skipping Year: '+year)
        print()
        #file.close()

def main():
    file = open("ICC_Rankings.csv","w")
    for i in range(2005,2017):
        file.write("Year,"+str(i)+"\n")
        print("Year,"+str(i))
        if i == 2009:
           read_web_page(str(i)+"-10", file)
        else:
           read_web_page(str(i), file)
    file.close()       
               

main()        
       
