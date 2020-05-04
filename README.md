# Pay2Gather bot
Bot for tracking expenses of the group during the journey or one-time occasion.

## Functionality
<ol><li>

With `/pay` or `/pay4` command user types in all the payments he did at one place. He provides a message in following format:
```text
/pay4 Dinner at Mc'Donalds, 2020-04-20
@username1 @username2, User3 (20+30+40)*0.9BYN
@username1 @username2 20BYN
```
where first line:
- TODO: who paid (optional, otherwise he's payer);
- description;
- date.

next lines:
- who was involved. Can be multiple persons, separated by "@" or ","
  - TODO: allow `all` to split between all members of gathering
- how much. Can be any mathematical expression, that split across involved users equally. 
- currency. BYN/$/€
</li><li>

In case the failures happen, bot replies with reported issue during parsing
- TODO: place where error happened
</li><li>

If message is valid, bot saves payment
- TODO: save to database
- TODO: track currency
</li><li>

If user edits message with payment, bot updates its responses
- TODO: update in DB as well. Edited payment should be upserted to DB
</li><li>

`/stats` returns "gatherness" and "expenses" of each user.

- Gatherness - how much user paid for others. Positive number - user paid for others more, then others for him. Negative - others paid for him.
- Expenses - how much each user spent during the gathering. Either he paid for himself or others paid for him.
</li></ol>

##Features:

1. TODO: On /finish put links to all the payments and refresh state
2. If user submitted a payment, remember his ID and username and use it for referencing on stats.
TODO: get user's info from mention, not only when he pays himself

