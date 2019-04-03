%%%-------------------------------------------------------------------
%%% @author MarcinS
%%% @copyright (C) 2018, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 27. lis 2018 17:33
%%%-------------------------------------------------------------------
-module(hello).
-author("MarcinS").

%% API
%%-export([hello_world/1]).
-compile(export_all).

hello_world(Name) -> io:fwrite("helo, \n").


debugWrite(StringFormat,ArgumentsList)->
  io:format(StringFormat,ArgumentsList),
  ok.

debugWrite(StringFormat)->
  io:format(StringFormat),
  ok.


watchdog_loop(Counter,TimeOut) ->
  case Counter of
    X when X rem 10000== 0 ->
      io:fwrite("Alive~n");
    _ -> ok
  end,
  receive
    _ -> watchdog_loop(Counter+1,TimeOut)
  after
    TimeOut-> io:fwrite("Nothing for ~wms can assume deadlock.",[TimeOut])
  end.

watchdogWelcome(TimeOut)->
  io:fwrite("Watchdog starded with value ~w~n",[TimeOut]),
  watchdog_loop(0,TimeOut).

watchdogStart()->
  register(wd,spawn(?MODULE,watchdogWelcome,[200])).

watchdogStart(TimeOut)->
  register(wd,spawn(?MODULE,watchdogWelcome,[TimeOut])).


prod_loop()->
  cb ! {take,self()},
  receive
    PID ->  PID ! {take,{dataAtom,self()}},
      wd ! {any},
      prod_loop()
  end.

prod_start()->
  io:fwrite("Producent starting with PID=~w~n",[self()]),
  prod_loop().

cons_loop()->
  cb ! {feed,self()},
  receive
    {dataAtom,N} ->
      wd ! {any},
      cons_loop();
      x -> debugWrite("got ~w instead of {dataAtom,<NUMBER>}",[x])
  end.

cons_start()->
  io:fwrite("Consumer starting with pid=~w~n",[self()]),
  cons_loop().


buffor_loop(List,Taken,Size)->
  case Taken of
    X when X==(Size+1) -> throw("putting into full buffor");
    _ -> ok
  end,
  receive
    {feed,PID} ->
      debugWrite("got feed in ~w, List is:~w~n",[self(),List]),
      [Item | Tail]= List,
      PID ! Item,
      case Taken of
        Size ->cb ! {doneOperatingFeed,{self(),Taken-1,Size},notfull};
        _ -> cb ! {doneOperatingFeed,{self(),Taken-1,Size}}
      end,
      buffor_loop(Tail,Taken -1,Size);
    {take,DATA} ->
      debugWrite("got take in ~w, and data is=~w List is:~w~n",[self(),DATA,List]),
      cb ! {doneOperatingTake,{self(),Taken+1,Size}},
        buffor_loop(List++[DATA],Taken+1,Size)
  end;

buffor_loop(List,-1,Size)->
  throw("taking from empty buffor").



startBuffor(K)->
  io:fwrite("singular buffor spawned with pid~w~n",[self()]),
  cb ! {self(),0,K},
  buffor_loop([],0,K).

startBuffors(0,K) ->
  ok;
startBuffors(N,K)->
    spawn(?MODULE,startBuffor,[K]),
    startBuffors(N-1,K).


%% szczegolny przypadek, kiedy nalezy zostac dodanym do innej kolejki
%% we are assuming SIZE > 1
centralBufforLoop({ReadableQue,0},{WritableQue,0})->
  debugWrite("centralBuffor match 0,0 RQ=~w WQ=~w~n",[ReadableQue,WritableQue]),
  receive
    {doneOperatingTake,{BuffPid,1,Size}}-> %% after getting elemnt it has 1 element. Has to be added to both Ques (wasn't in readable, )
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,1,Size}}]),
      centralBufforLoop({queue:in({BuffPid,1,Size},ReadableQue),0+1},{queue:in({BuffPid,1,Size},WritableQue),0+1});
    {doneOperatingTake,{BuffPid,Size,Size}}->
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,Size,Size}}]),
      centralBufforLoop({ReadableQue,0},{WritableQue,0}); %% match when buffor is full, and cannot be added
    {doneOperatingTake,{BuffPid,Taken,Size}}-> %% since top match didn't match, we can assume buffor is not full
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,Taken,Size}}]),
      WritableQue2=queue:in({BuffPid,Taken,Size},WritableQue),
      centralBufforLoop({ReadableQue,0},{WritableQue2,0+1});

    {doneOperatingFeed,{BuffPid,Taken,Size},notfull}-> %% similarrly full element when becoming not full, has to be placed in both Ques
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,Taken,Size},notfull}]),
      centralBufforLoop({queue:in({BuffPid,Taken,Size},ReadableQue),0+1},{queue:in({BuffPid,Taken,Size},WritableQue),0+1});
    {doneOperatingFeed,{BuffPid,0,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,0,Size}}]),
      centralBufforLoop({ReadableQue,0},{WritableQue,0}); %% match when buffor is empty and cannot be added to ReadableQue
    {doneOperatingFeed,{BuffPid,Taken,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,Taken,Size}}]),
      ReadableQue2=queue:in({BuffPid,Taken,Size},ReadableQue),
      centralBufforLoop({ReadableQue2,0+1},{WritableQue,0})
  end;


centralBufforLoop({ReadableQue,0},{WritableQue,WQSize})->
  debugWrite("centralBuffor match 0,_ RQ=~w WQ=~w~n",[ReadableQue,WritableQue]),
  receive
    {take,PID}->
      debugWrite("got ~w~n",[{take,PID}]),
      {{value,{BuffPid,Taken,Size}},WritableQue2}=queue:out(WritableQue),
      PID ! BuffPid,
      centralBufforLoop({ReadableQue,0},{WritableQue2,WQSize-1});

    {doneOperatingTake,{BuffPid,1,Size}}-> %% after getting elemnt it has 1 element. Has to be added to both Ques (wasn't in readable, )
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,1,Size}}]),
      centralBufforLoop({queue:in({BuffPid,1,Size},ReadableQue),0+1},{queue:in({BuffPid,1,Size},WritableQue),WQSize+1});
    {doneOperatingTake,{BuffPid,Size,Size}}->
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,Size,Size}}]),
      centralBufforLoop({ReadableQue,0},{WritableQue,WQSize}); %% match when buffor is full, and cannot be added
    {doneOperatingTake,{BuffPid,Taken,Size}}-> %% since top match didn't match, we can assume buffor is not full
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,Taken,Size}}]),
      WritableQue2=queue:in({BuffPid,Taken,Size},WritableQue),
      centralBufforLoop({ReadableQue,0},{WritableQue2,WQSize+1});

    {doneOperatingFeed,{BuffPid,Taken,Size},notfull}-> %% similarrly full element when becoming not full, has to be placed in both Ques
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,Taken,Size},notfull}]),
      centralBufforLoop({queue:in({BuffPid,Taken,Size},ReadableQue),0+1},{queue:in({BuffPid,Taken,Size},WritableQue),WQSize+1});
    {doneOperatingFeed,{BuffPid,0,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,0,Size}}]),
      centralBufforLoop({ReadableQue,0},{WritableQue,WQSize}); %% match when buffor is empty and cannot be added to ReadableQue
    {doneOperatingFeed,{BuffPid,Taken,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,Taken,Size}}]),
      ReadableQue2=queue:in({BuffPid,Taken,Size},ReadableQue),
      centralBufforLoop({ReadableQue2,1},{WritableQue,WQSize+1})
  end;



centralBufforLoop({ReadableQue,RQSize},{WritableQue,0})->
  debugWrite("centralBuffor match _,0 RQ=~w WQ=~w~n",[ReadableQue,WritableQue]),
  receive
    {feed,PID}->
      debugWrite("got ~w~n",[{feed,PID}]),
      {{value,{BuffPid,Taken,Size}},ReadableQue2}=queue:out(ReadableQue),
      BuffPid ! {feed,PID},
      centralBufforLoop({ReadableQue2,RQSize-1},{WritableQue,0});

    {doneOperatingTake,{BuffPid,1,Size}}-> %% after getting elemnt it has 1 element. Has to be added to both Ques (wasn't in readable, )
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,1,Size}}]),
      centralBufforLoop({queue:in({BuffPid,1,Size},ReadableQue),RQSize+1},{queue:in({BuffPid,1,Size},WritableQue),0+1});
    {doneOperatingTake,{BuffPid,Size,Size}}->
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,Size,Size}}]),
      centralBufforLoop({ReadableQue,RQSize},{WritableQue,0}); %% match when buffor is full, and cannot be added
    {doneOperatingTake,{BuffPid,Taken,Size}}-> %% since top match didn't match, we can assume buffor is not full
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,Taken,Size}}]),
      WritableQue2=queue:in({BuffPid,Taken,Size},WritableQue),
      centralBufforLoop({ReadableQue,RQSize},{WritableQue2,0+1});

    {doneOperatingFeed,{BuffPid,Taken,Size},notfull}-> %% similarrly full element when becoming not full, has to be placed in both Ques
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,Taken,Size},notfull}]),
      centralBufforLoop({queue:in({BuffPid,Taken,Size},ReadableQue),RQSize+1},{queue:in({BuffPid,Taken,Size},WritableQue),0+1});
    {doneOperatingFeed,{BuffPid,0,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,0,Size}}]),
      centralBufforLoop({ReadableQue,RQSize},{WritableQue,0}); %% match when buffor is empty and cannot be added to ReadableQue
    {doneOperatingFeed,{BuffPid,Taken,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed ,{BuffPid,Taken,Size}}]),
      ReadableQue2=queue:in({BuffPid,Taken,Size},ReadableQue),
      centralBufforLoop({ReadableQue2,RQSize+1},{WritableQue,0})

end;




centralBufforLoop ({ReadableQue,RQSize},{WritableQue,WQSize})->
  debugWrite("centralBuffor match _,_ RQ=~w WQ=~w~n",[ReadableQue,WritableQue]),
  receive
    {feed,PID}->
      debugWrite("got ~w~n",[{feed,PID}]),
      {{value,{BuffPid,Taken,Size}},ReadableQue2}=queue:out(ReadableQue),
      BuffPid ! {feed,PID},
      centralBufforLoop({ReadableQue2,RQSize-1},{WritableQue,WQSize});
    {take,PID}->
      debugWrite("got ~w~n",[{take,PID}]),
      {{value,{BuffPid,Taken,Size}},WritableQue2}=queue:out(WritableQue),
      PID ! BuffPid,
      centralBufforLoop({ReadableQue,RQSize},{WritableQue2,WQSize-1});

    {doneOperatingTake,{BuffPid,1,Size}}-> %% after getting elemnt it has 1 element. Has to be added to both Ques (wasn't in readable, )
      debugWrite("got ~w~n",[{doneOperatingTake,{BuffPid,1,Size}}]),
      centralBufforLoop({queue:in({BuffPid,1,Size},ReadableQue),RQSize+1},{queue:in({BuffPid,1,Size},WritableQue),WQSize+1});
    {doneOperatingTake,{BuffPid,Size,Size}}->
      debugWrite("got ~w~n",[{doneOperatingTake ,{BuffPid,Size,Size}}]),
      centralBufforLoop({ReadableQue,RQSize},{WritableQue,WQSize}); %% match when buffor is full, and cannot be added
    {doneOperatingTake,{BuffPid,Taken,Size}}-> %% since top match didn't match, we can assume buffor is not full
      debugWrite("got ~w~n",[{doneOperatingTake ,{BuffPid,Taken,Size}}]),
      WritableQue2=queue:in({BuffPid,Taken,Size},WritableQue),
      centralBufforLoop({ReadableQue,RQSize},{WritableQue2,WQSize+1});

    {doneOperatingFeed,{BuffPid,Taken,Size},notfull}-> %% similarrly full element when becoming not full, has to be placed in both Ques
      debugWrite("got ~w~n",[{doneOperatingFeed,{BuffPid,Taken,Size},notfull}]),
      centralBufforLoop({queue:in({BuffPid,Taken,Size},ReadableQue),RQSize+1},{queue:in({BuffPid,Taken,Size},WritableQue),WQSize+1});
    {doneOperatingFeed,{BuffPid,0,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed ,{BuffPid,0,Size}}]),
      centralBufforLoop({ReadableQue,RQSize},{WritableQue,WQSize}); %% match when buffor is empty and cannot be added to ReadableQue
    {doneOperatingFeed,{BuffPid,Taken,Size}} ->
      debugWrite("got ~w~n",[{doneOperatingFeed ,{BuffPid,Taken,Size}}]),
      ReadableQue2=queue:in({BuffPid,Taken,Size},ReadableQue),
      centralBufforLoop({ReadableQue2,RQSize+1},{WritableQue,WQSize})
  end.



centralBufforSendLive(N,PID)->
  PID ! live,
  centralBufforAwait(N,[]).

centralBufforAwait(0,List)->
  io:fwrite("CentralBuffor~w State ~w,~w~n",[self(),0,List]),
  io:fwrite("starting central buffor with arguments (~w,~w)",[{queue:new(),0},{queue:from_list(List),length(List)}]),
  centralBufforLoop({queue:new(),0},{queue:from_list(List),length(List)});

centralBufforAwait(N,List)->
  io:fwrite("CentralBuffor~w State  ~w,~w~n",[self(),N,List]),
  receive
    {Pid,0,K} ->
      centralBufforAwait(N-1,lists:append(List,[{Pid,0,K}]))
  end.




centralBufforStart(N,K)->
  register(cb,spawn(?MODULE,centralBufforSendLive,[N,self()])),
  receive
    live -> ok
  end,
  startBuffors(N,K).


start()->
      centralBufforStart(5,2),
      watchdogStart(1000),
      [spawn(?MODULE,prod_start,[]) || _ <- lists:seq(1,3)],
      [spawn(?MODULE,cons_start,[]) || _ <- lists:seq(1,3)],
      timer:sleep(infinity).





























%%centralBuffor_loop(buffList)->
%%
%%prod_loop()->
%%  cb ! {take,self()},
%%  receive
%%    PID ->  PID ! {take,{dataAtom,self()}},
%%      prod_loop()
%%  end.
%%
%%prod_start()->
%%  io:fwrite("Producent starting with PID=~w~n",[self()]),
%%  prod_loop().
%%
%%cons_loop()->
%%  cb ! {feed,self()},
%%  receive
%%    {dataAtom,N} -> cons_loop();
%%    x -> debugWrite("got ~w instead of {dataAtom,<NUMBER>}",[x])
%%  end.
%%
%%cons_start()->
%%  io:fwrite("Consumer starting with pid=~w~n",[self()]),
%%  cons_loop().
%%
%%
%%buffor_loop(List,Taken)->
%%
%%  receive
%%    {feed,PID} ->
%%      debugWrite("got feed in ~w, List is:~w~n",[self(),List]),
%%      [Item | Tail]= List,
%%      PID ! Item,
%%      cb ! {doneOperating,self()},
%%      buffor_loop(Tail,Taken -1);
%%    {take,DATA} ->
%%      debugWrite("got take in ~w, and data is=~w List is:~w~n",[self(),DATA,List]),
%%      cb ! {doneOperating,self()},
%%        buffor_loop(List++[DATA],Taken+1)
%%  end.
%%
%%
%%startBuffor(K)->
%%  io:fwrite("singular buffor spawned with pid~w~n",[self()]),
%%  cb ! {self(),0,K},
%%  buffor_loop([],0).
%%
%%
%%startBuffors(N,K)->
%%  case N of
%%      X when X  >0 ->
%%        spawn(?MODULE,startBuffor,[K]),
%%        startBuffors(N-1,K);
%%      X when X==0 -> ok
%%  end.
%%
%%
%%filterOutEmpty(Item)->
%%  case Item of
%%    {Pid,0,K} -> false;
%%    _ -> true
%%  end.
%%
%%filterOutFull(Item)->
%%  case Item of
%%    {Pid,K1,K2} when (K1 == K2) -> false
%%  end,
%%  true.
%%
%%filterOnlyFull(Item)->
%%  case filterOutFull(Item) of
%%    true -> false;
%%    false -> true
%%  end.
%%
%%filterOnlyEmpty(Item)->
%%  case filterOutEmpty(Item) of
%%    true -> false;
%%    false -> true
%%  end.
%%
%%
%%
%%
%%recievedDoneOperating(TupleData,ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize)->
%%  {PID_buff,Taken,Total}=TupleData,
%%  debugWrite("recieved DoneOperating sush as ~w~n",[TupleData]),
%%  case Taken of
%%    Total ->
%%      {ListNotFull,
%%      ListNotEmpty++[{PID_buff,Taken,Total}],
%%      lists:delete({PID_buff,Taken,Total}, ListNotReady),
%%      ListNotFullSize,
%%      ListNotEmptySize+1,
%%      ListNotReadySize-1};
%%    0 ->
%%      {ListNotFull++[{PID_buff,Taken,Total}],
%%      ListNotEmpty,
%%      lists:delete({PID_buff,Taken,Total}, ListNotReady),
%%      ListNotFullSize+1,
%%      ListNotEmptySize,
%%      ListNotReadySize-1};
%%    _ ->
%%    {ListNotFull++[{PID_buff,Taken,Total}],
%%      ListNotEmpty++[{PID_buff,Taken,Total}],
%%      lists:delete({PID_buff,Taken,Total}, ListNotReady),
%%      ListNotFullSize+1,
%%      ListNotEmptySize+1,
%%      ListNotReadySize-1}
%%  end.
%%
%%
%%receivedFeed(Pid,ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize)->
%%  [{PID_buff,Taken,Total} | TailNotEmpty ]= ListNotEmpty,
%%  TailNotFull= lists:delete({PID_buff,Taken,Total},ListNotFull),
%%  PID_buff ! {feed,Pid},
%%  debugWrite("recieved Feed from ~w and ~w ! ~w~n",[Pid,PID_buff,{feed,Pid}]),
%%  {TailNotFull,TailNotEmpty,ListNotReady++[{PID_buff,Taken-1,Total}],length(TailNotFull),ListNotEmptySize-1,ListNotReadySize+1}.
%%
%%receivedTake(Pid,ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize)->
%%  [{PID_buff,Taken,Total} | TailNotFull ]= ListNotFull,
%%  TailNotEmpty=lists:delete({PID_buff,Taken,Total},ListNotEmpty),
%%  Pid ! PID_buff,
%%  debugWrite("recieved Take from ~w and ~w ! ~w~n",[Pid,Pid,PID_buff]),
%%  {TailNotFull,TailNotEmpty,ListNotReady++[{PID_buff,Taken+1,Total}],ListNotFullSize-1,length(TailNotEmpty),ListNotReadySize+1}.
%%
%%
%%
%%
%%
%%
%%centralBufforLoop(ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize,BuffCount)->
%%  debugWrite("central Buffor runing with pid=~w ListNotFull=~w ListNotEmpty=~w ListNotReady=~w~nand sizes ListNotFull=~w,ListNotEmpty=~w,ListNotReady~w~n"
%%    ,[self(),ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize]),
%%  %% important note. Cases are MUTUALLY EXCLUSIVE, except the last one. for example {0,0,x} where x!=BuffCount is not possible.
%%  case {ListNotFullSize,ListNotEmptySize,ListNotReadySize} of
%%    {_,_,BuffCount} ->
%%      debugWrite("   CASE 1   "),
%%      receive
%%        {doneOperating,BUFF_PID} ->
%%          [{PID_buff,Taken,Total} | TailNotReady ] = lists:filter(fun({Lambda_pid,_,_})-> Lambda_pid ==BUFF_PID  end,ListNotReady),
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%            recievedDoneOperating({PID_buff,Taken,Total},ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount)
%%      end;
%%
%%    {0,_,_}->
%%      debugWrite("   CASE 2   "),
%%      receive
%%        {doneOperating,BUFF_PID} ->
%%          [{PID_buff,Taken,Total} | TailNotReady ] = lists:filter(fun({Lambda_pid,_,_})-> Lambda_pid ==BUFF_PID  end,ListNotReady),
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%            recievedDoneOperating({PID_buff,Taken,Total},ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount);
%%        {feed,PID} ->
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%             receivedFeed(PID,ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount)
%%      end;
%%    {_,0,_}->
%%      debugWrite("   CASE 3   "),
%%      receive
%%        {doneOperating,BUFF_PID} ->
%%          [{PID_buff,Taken,Total} | TailNotReady ] = lists:filter(fun({Lambda_pid,_,_})-> Lambda_pid ==BUFF_PID end,ListNotReady),
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%          recievedDoneOperating({PID_buff,Taken,Total},ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount);
%%        {take,PID} ->
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%          receivedTake(PID,ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount)
%%      end;
%%    {_,_,_}->
%%      debugWrite("   CASE 4   "),
%%      receive
%%        {doneOperating,BUFF_PID} ->
%%          [{PID_buff,Taken,Total} | TailNotReady ] = lists:filter(fun({Lambda_pid,_,_})-> Lambda_pid ==BUFF_PID end,ListNotReady),
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%            recievedDoneOperating({PID_buff,Taken,Total},ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount);
%%        {take,PID} ->
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%            receivedTake(PID,ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount);
%%        {feed,PID} ->
%%          {ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_}=
%%          receivedFeed(PID,ListNotFull,ListNotEmpty,ListNotReady,ListNotFullSize,ListNotEmptySize,ListNotReadySize),
%%          centralBufforLoop(ListNotFull_,ListNotEmpty_,ListNotReady_,ListNotFullSize_,ListNotEmptySize_,ListNotReadySize_,BuffCount)
%%      end
%%
%%end.
%%
%%%% feed as feed me, so take
%%%% take as take from me.
%%%% our prod/cons are selfish *******
%%%%centralBufforLoop(List,MaxPlaces,Taken)->
%%%%  case {MaxPlaces,Taken} of
%%%%       {MP,T} when (T>0) and (T<MP)->
%%%%        receive
%%%%          {feed,PID} ->
%%%%%%            io:fwrite("in feed1,PID~n"),
%%%%            [{PID_BUFF,TakenLocal,Total} | Void] =  lists:filter(fun({Pid,K1,K2,true})-> (K1>0) end,List),
%%%%            Tail = lists:delete({PID_BUFF,TakenLocal,Total},List),
%%%%            PID_BUFF ! {feed,PID} , %% we send to buffor, PID of consumer demanding action, so he can send data
%%%%              io:fwrite("send to~w (~w) and MP=~w T=~w~n",[PID_BUFF,{feed,PID},MP,T]),
%%%%              centralBufforLoop(
%%%%                lists:append(Tail,[{PID_BUFF,TakenLocal-1,Total}])
%%%%                ,MaxPlaces, Taken-1);
%%%%              %% we rotate Que arbitrary, first element goes to the end of que after every operation
%%%%          {take,PID} ->
%%%%%%            io:fwrite("in take1~n"),
%%%%            [{PID_BUFF,TakenLocal,Total} | Void] =  lists:filter(fun({Pid,K1,K2,true}) -> K1/=K2 end,List),
%%%%            %%io:fwrite("after filter~n"),
%%%%            Tail = lists:delete({PID_BUFF,TakenLocal,Total},List),
%%%%            %%io:fwrite("after delete~n"),
%%%%            PID ! PID_BUFF , %% we send to producer, where to send data
%%%%            io:fwrite("send ~w~n",[PID_BUFF]),
%%%%            centralBufforLoop(
%%%%              lists:append(Tail,[{PID_BUFF,TakenLocal+1,Total}])
%%%%              ,MaxPlaces, Taken+1)
%%%%        %% we rotate Que arbitrary, first element goes to the end of que after every operation
%%%%        end;
%%%%%%     no free spaces
%%%%      {MP,T} when T==MP ->
%%%%        receive
%%%%          {feed,PID} ->
%%%%%%            io:fwrite("in feed2~n"),
%%%%            [{PID_BUFF,TakenLocal,Total,true} | Void] =  lists:filter(fun({Pid,K1,K2,true})-> (K1>0) end,List),
%%%%            Tail = lists:delete({PID_BUFF,TakenLocal,Total},List),
%%%%            PID_BUFF ! {feed,PID} , %% we send to buffor, PID of consumer demanding action, so he can send data
%%%%            io:fwrite("send to ~w  (~w) and MP=~w T=~w ~n",[PID_BUFF,{feed,PID},MP,T]),
%%%%            centralBufforLoop(
%%%%              lists:append(Tail,[{PID_BUFF,TakenLocal-1,Total,false}])
%%%%              ,MaxPlaces, Taken-1)
%%%%        end;
%%%%%%    no taken, all free
%%%%      {MP,T} when T==0 ->
%%%%        receive
%%%%          {take,PID} ->
%%%%%%            io:fwrite("in take2~n"),
%%%%            [{PID_BUFF,TakenLocal,Total} | Void] =  lists:filter(fun({Pid,K1,K2,true}) -> K1/=K2 end,List),
%%%%            Tail = lists:delete({PID_BUFF,TakenLocal,Total},List),
%%%%            PID ! PID_BUFF , %% we send to producer, where to send data
%%%%            io:fwrite("send ~w~n",[PID_BUFF]),
%%%%            centralBufforLoop(
%%%%              lists:append(Tail,[{PID_BUFF,TakenLocal+1,Total,false}])
%%%%              ,MaxPlaces, Taken+1)
%%%%        %% we rotate Que arbitrary, first element goes to the end of que after every operation
%%%%        end;
%%%%      _ -> ok
%%%%  end.
%%
%%
%%centralBufforSpawnMessage(N,List,TotalFree,PID)->
%%  io:fwrite("Central buffor PID=~w~n",[self()]),
%%  PID ! live,
%%  centralBufforAwait(N,List,TotalFree).
%%
%%centralBufforAwait(N,List,TotalFree)->
%%  io:fwrite("CentralBuffor State ~w,~w,~w~n",[N,List,TotalFree]),
%%  case N of
%%    X when X==0 -> centralBufforLoop(List,[],[],length(List),0,0,length(List));
%%    X when X/=0 ->
%%      receive
%%        {Pid,0,K} ->
%%          centralBufforAwait(N-1,lists:append(List,[{Pid,0,K}]),TotalFree+K)
%%      end
%%  end.
%%
%%
%%centralBufforStart(N,K)->
%%  register(cb,spawn(?MODULE,centralBufforSpawnMessage,[N,[],0,self()])),
%%  receive
%%    live -> ok
%%  end,
%%  startBuffors(N,K).
%%
%%
%%
%%
%%
%%
%%
%%
%%start()->
%%      centralBufforStart(5,2),
%%      [spawn(?MODULE,prod_start,[]) || _ <- lists:seq(1,3)],
%%      [spawn(?MODULE,cons_start,[]) || _ <- lists:seq(1,3)],
%%      timer:sleep(infinity).
%%    A = [1,2,3,4],
%%    B= lists:delete(5,A),
%%    C = lists:delete(2,A),
%%    io:fwrite("A=~w~nB=~w~nC=~w~n",[A,B,C]).









































%%
%%a_loop()->
%%
%%  c ! "aaa",
%%  a_loop().
%%b_loop()->
%%  c ! "bbb",
%%  b_loop().
%%
%%a2_loop(NR)->
%%  case NR of
%%    1000 -> ok;
%%    _ -> c! {"aaa",NR}, a2_loop(NR+1)
%%  end.
%%
%%b2_loop(NR)->
%%  case NR of
%%    1000 -> ok;
%%    _ -> c! {"bbb",NR}, b2_loop(NR+1)
%%  end.


%%c_loop()->
%%  receive
%%    "aaa"->io:fwrite("aaa~n")
%%  end,
%%  receive
%%    "bbb"->io:fwrite("bbb~n")
%%  end,
%%  c_loop().
%%c_loop()->
%%  receive
%%    "aaa"->io:fwrite("aaa~n");
%%    "bbb"->io:fwrite("bbb~n")
%%  end,
%%  c_loop().
%%
%%c2_loop()->
%%  receive
%%    {"aaa",NR} -> io:fwrite("aaa~B~n",[NR]);
%%    {"bbb",NR} -> io:fwrite("bbb~B~n",[NR])
%%  end,
%%  c2_loop().
%%
%%c3_loop()->
%%  receive
%%    _ -> ok
%%  end,
%%  c3_loop().
%%
%%
%%prod_loop()->
%%  b ! {take,self(), "data"},
%%  receive
%%    ack -> prod_loop();
%%    _ -> io:fwrite("unexcepceted messsage exiiting"),
%%      ok
%%  end.
%%
%%prod_start()->
%%  io:fwrite("Prod starting nr:~w~n",[self()]),
%%  prod_loop().
%%
%%
%%cons_loop()->
%%  b ! {feed,self()},
%%  receive
%%    Data -> io:fwrite("~w~n",[Data])
%%  end,
%%  cons_loop().
%%
%%cons_start()->
%%  io:fwrite("Cons starting nr:~w~n",[self()]),
%%  cons_loop().
%%
%%
%%buffor_loop(List,Count,Size)
%%  when (Count<Size) and ( Count > 0 ) ->
%%  %io:fwrite("at least one element lives !!!!~n"),
%%  receive
%%    {take, Pid,Data} -> Pid ! ack,
%%      buffor_loop(List ++ [Data],Count+1,Size);
%%    {feed,Pid} -> Pid ! lists:nth(1,List),
%%      buffor_loop(lists:sublist(List,2,Size),Count-1,Size)
%%  end;
%%
%%
%%buffor_loop(List,Count,Size)
%%  when Count == 0 ->
%%  receive
%%    {take, Pid,Data} -> Pid ! ack,
%%      buffor_loop(List ++ [Data],Count+1,Size)
%%  end;
%%
%%buffor_loop(List,Count,Size)
%%  when Count >= Size ->
%%  receive
%%    {feed,Pid} -> Pid ! lists:nth(1,List),
%%      buffor_loop(lists:sublist(List,2,Size),Count-1,Size)
%%  end.
%%
%%
%%
%%
%%
%%buffor_start()->
%%  io:fwrite("buffor started~n"),
%%  buffor_loop([],0,100).
%%
%%
%%start() ->
%%  register(b,spawn(?MODULE,buffor_start,[])),
%%  [spawn(?MODULE,prod_start,[]) || _ <- lists:seq(1,10)],
%%  [spawn(?MODULE,cons_start,[]) || _ <- lists:seq(1,10)],
%%
%%  timer:sleep(infinity).
%%

%%
%%start() ->
%%  register(a,spawn(?MODULE,a2_loop,[0])),
%%  register(b,spawn(?MODULE,b2_loop,[0])),
%%  register(c,spawn(?MODULE,c2_loop,[])),
%%  timer:sleep(infinity).




