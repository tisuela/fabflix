import sys

def main(fileName):
    print(f"Processing input file: {fileName}")
    logFile = open(fileName, "r")

    totalTime  = 0
    queryCount = 0

    nsTime = logFile.readline()

    while nsTime:

        msTime = float(nsTime)/1000000

        totalTime  += msTime
        queryCount += 1

        nsTime = logFile.readline()
    

    average = totalTime/queryCount
    print(f"Average of {fileName} is: {average}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        main(sys.argv[1])
    

    
