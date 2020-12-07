import matplotlib
import matplotlib.pyplot as plt
import numpy as np

#####################   Load Data   ##################################
import csv

timestamp = []
setpoint = []
lux = []
slack = []
intensity = []
counter = []
wattUsage = []
error = []

# Timestamp, Setpoint, Lux, Slack, Intensity, Counter, WattUsage
with open('MovieData.csv') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        if line_count < 550 or line_count > 950:
            print(f'Column names are {", ".join(row)}')
            line_count += 1
        else:
            # included values
            timestamp.append(row[0])
            setpoint.append(int(row[1]))
            lux.append(int(row[2]))
            slack.append(int(row[3]))
            intensity.append(int(row[4]))
            counter.append(int(row[5]))
            wattUsage.append(float(row[6]))

            # calculated values
            error.append(int(row[2]) - int(row[1]))

            # increment
            line_count += 1
    print(f'Processed {line_count} lines.')



################### PREPARE DATA #######################
# error = []
# time = []
# intensity = []
# 
# # Get error
# for i in range(0, len(timestamp)):
#     #print((lux_formula_value, timestamp, setpoint, light_red))
#     if light_red == 0 and lux_formula_value > setpoint:
#         # Natural light level too high
#         continue
#     elif abs(lux_formula_value - setpoint) > 10:
#         # Outliers
#         continue
#     else:
#         # lux above setpoint -> positive error
#         error.append(lux_formula_value - setpoint)
#         time.append(datetime.fromtimestamp(int(timestamp) // 1000000000))
#         intensity.append(light_red)
# 
# # convert python date objects to matplotlib dates
# dates = matplotlib.dates.date2num(time)
# 


fig, ax = plt.subplots()

################# ERROR ##############3
color = 'tab:blue'
ax.set_ylabel('Error [Lux]', color=color)
error_line, = ax.step(counter, error, color=color, label='Error')
#ax.xaxis_date()
#plt.gcf().autofmt_xdate()
plt.ylim(top=max(error), bottom=min(error))
ax.set(xlabel='Message Count [int]',
       title='Error over time')
ax.grid()

################# INTENSITY ######################
ax2 = ax.twinx()
color = 'tab:red'
ax2.set_ylabel('Intensity [0-255]', color=color)  # we already handled the x-label with ax1
intensity_line, = ax2.step(counter, intensity, color=color, label='Intensity')
#ax2.tick_params(axis='y', labelcolor=color)
plt.ylim(top=260, bottom=-10)
plt.yticks(np.arange(0, 260, 25))

#####################################################
# fig.savefig("test.png")

# fig.legend(loc='upper left')
plt.legend(handles=[error_line, intensity_line], bbox_to_anchor=(1.05, 1), loc='upper left',
           borderaxespad=0.)
fig.tight_layout()
fig.set_size_inches(20, 5)
plt.savefig("plot.svg", bbox_inches = "tight") # 'tight' makes room for x-axis labels
plt.show()  # Must be called last since this clears the figure, resulting in a white svg.


print('number of obs: ' + str(len(error)))
print('SD: ' + str(np.std(error)))
print('Avg: ' + str(np.average(error)))




np.histogram(error)
fig, ax = plt.subplots()
n, bins, patches = ax.hist(error, range(-10, 11, 2))
ax.set(xlabel='Error [Lux]', ylabel='Frequency',
       title='Lux error distribution')
fig.tight_layout()
fig.set_size_inches(5, 5)
plt.savefig("hist.svg", bbox_inches = "tight") # 'tight' makes room for x-axis labels
plt.show()