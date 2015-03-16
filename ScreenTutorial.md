# Introduction #

The page describes the details of the game screens.





# Quick research #

The **Quick research** is an overlay panel accessible from level 3 (commander) by left clicking on the flask icon in the top-right part of the screen. (If you don't see the flask icon, go to the **Options > Screen** and select the **Show quick research/production** checkbox.

![http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-quick-research-01.png](http://open-ig.googlecode.com/svn/trunk/open-ig/doc/open-ig-quick-research-01.png)

You don't need to hold the left mouse button to issue commands, but holding and releasing the mouse over a research label or the stop button will issue the command.

The panel is divided into four sections.

## Current research info ##

The top section shows details of the currently running research. (If no current research is running, it simply displays a gray text of this fact.)

The progress also indicates if any issues exist with the current research such as lack of money or lack of lab capacity.

The blue button allows you to set the money allocated to the current research the same way as on the research screen. Clicking or holding the mouse button down will increase or decrease the money allocation (visible as a ratio to the left of the button). The click is location sensitive, i.e., clicking near the left or the right side will increase the adjustment speed of the money.

The stop button lets cancel the current research (no progress is lost).

## Available research ##

This section lists those research options which have all their prerequisite technologies available. The labels are color coded the same way as the Information screen / Inventions tab:

  * Yellow: research started, the percentage shows how far it has progressed
  * Light blue: the current lab count and configuration allows the complete research of the technology
  * Light green: enough planets exist, but you need to build and/or replace labs.
  * Dark green: you don't have enough planets to complete this research even if you built labs

Hovering over a label will display information and requirements about it in the bottom two sections.

Click on a label to start/continue the research with that technology. Clicking on a label will close the panel. (Note: the current research is not listed in the columns).

The screenshot above shows 4 main categories (ships, equipment, weapons, buildings). Depending on the remaining research, columns without entries won't get shown.

## Description ##

This section shows the long name of the research along with a few sentence about its purpose. If you don't hover anything, it will show the details of the current research (if any). Once you hover over a label in the section above, it will show the details of that technology. In this case, the maximum research cost is displayed along the name.

## Requirements ##

The bottom section shows a comparison of the current available and required laboratories of the current or hovered technology.

Yellow coloring in the available line indicates that some lab of the specified kind is not (yet) operational.

Red coloring in the required line indicates that you don't have enough labs built of that particular kind.

If no current research is going on and nothing is hovered, the required line might not show up.

Finally, the last light gray line offers some textual tips of the current available/required labs, e.g.,

  * you have enough labs built but not all of them are functioning,
  * you have enough planets but you need to build/replace labs,
  * you need to colonize/conquer more planets to be able to build more labs.