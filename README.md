# Tasklist
One of the projects with JetBrains academy, list of tasks.

It's basic console program that asks you to input date, time, priority and tasks. You can add, print, edit or delete tasks. It all looks like this:<br />

<img width="450" alt="Run" src="https://github.com/Sonofasleep/Tasklist/blob/master/Run%20screeenshot.png">

P column stands for "priority" and means next:<br />
C - "critical" - red color;<br />
H - "hot task" - yellow color;<br />
N - "normal priority" - green color;<br />
L - "low pririoty" - blue color.<br />

D column stands for "Due" and calculates from current system date and task date. It means next:<br />
Green color - task date in future;<br />
Yellow color - task is today;<br />
Red color - task is outdated.<br />

On start, program loads JSON file with all information and updates file on end.
