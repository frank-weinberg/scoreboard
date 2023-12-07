$(initialize);

function initialize() {
  'use strict';

  WS.Register('ScoreBoard.CurrentGame.Team(*).StarPass', function (k, v) {
    $('[Team=' + k.Team + '] [Position="Jammer"]').toggleClass('Jamming', !isTrue(v));
    $('[Team=' + k.Team + '] [Position="Pivot"]').toggleClass('Jamming', isTrue(v));
  });

  $(document).keyup(function (e) {
    switch (e.which) {
      case 74: // j
        toggleSetting('ShowJammers');
        break;
      case 76: // l
        toggleSetting('ShowLineups');
        break;
      case 78: // n
        toggleSetting('ShowAllNames');
        break;
      case 67: // c
        toggleSetting('Clock');
        break;
      case 83: // s
        toggleSetting('Score');
        break;
      case 48: // 0
        togglePanel('PPJBox');
        break;
      case 49: // 1
        togglePanel('RosterTeam1');
        break;
      case 50: // 2
        togglePanel('RosterTeam2');
        break;
      case 51: // 3
        togglePanel('PenaltyTeam1');
        break;
      case 52: // 4
        togglePanel('PenaltyTeam2');
        break;
      case 57: // 9
        togglePanel('LowerThird');
        break;
      case 85: // u
        togglePanel('Upcoming');
        break;
      case 32: // space
        WS.Set('ScoreBoard.Settings.Setting(Overlay.Interactive.Panel)', '');
        break;
    }
  });

  WS.AutoRegister();
  WS.Connect();

  /*  WS.Register('ScoreBoard.Settings.Setting(Overlay.Interactive.Panel)', function (k, v) {
    $('.OverlayPanel').removeClass('Show');
    switch (v) {
      case 'RosterTeam1':
        $('[Team="1"] .OverlayPanel.RosterTeam').addClass('Show');
        break;
      case 'RosterTeam2':
        $('[Team="2"] .OverlayPanel.RosterTeam').addClass('Show');
        break;
      case 'PenaltyTeam1':
        $('[Team="1"] .OverlayPanel.PenaltyTeam').addClass('Show');
        break;
      case 'PenaltyTeam2':
        $('[Team="2"] .OverlayPanel.PenaltyTeam').addClass('Show');
        break;
      default:
        $('.OverlayPanel.' + v).addClass('Show');
        break;
    }
  });*/

  setTimeout(function () {
    $('body').removeClass('preload');
  }, 1000);
}

function toggleSetting(s) {
  WS.Set(
    'ScoreBoard.Settings.Setting(Overlay.Interactive.' + s + ')',
    WS.state['ScoreBoard.Settings.Setting(Overlay.Interactive.' + s + ')'] === 'On' ? 'Off' : 'On'
  );
}

function togglePanel(p) {
  WS.Set(
    'ScoreBoard.Settings.Setting(Overlay.Interactive.Panel)',
    WS.state['ScoreBoard.Settings.Setting(Overlay.Interactive.Panel)'] === p ? '' : p
  );
}

function toBackground(k, v) {
  return v || 'transparent';
}

function toPercent(k, v) {
  return v + '%';
}

function toIndicator(k, v) {
  'use strict';
  var prefix = k.substring(0, k.lastIndexOf('.'));
  return isTrue(WS.state[prefix + '.StarPass'])
    ? 'SP'
    : isTrue(WS.state[prefix + '.Lost'])
    ? ''
    : isTrue(WS.state[prefix + '.Lead'])
    ? '★'
    : '';
}

function toPpjColumnHeight(k, v) {
  'use strict';
  return v * 4 + 'px';
}

function toPpjColumnWidth() {
  'use strict';
  var ne1 = $('.PPJBox .Team1 .GraphBlock').length;
  var ne2 = $('.PPJBox .Team2 .GraphBlock').length;
  if (ne2 > ne1) {
    ne1 = ne2;
  }
  var nel = ne1 + 3;
  var wid = parseInt($('.PPJBox').innerWidth());
  var newWidth = parseInt(wid / nel) - 3;
  $('.ColumnWidth').css('width', newWidth);

  return newWidth;
}

function toPpjMargin(k, v) {
  'use strict';
  return parseInt($('.PPJBox .Team1 .Period').innerHeight()) - v * 4;
}

function toLowerThirdColorFg(k, v) {
  return toLowerThirdColor('overlay.fg');
}

function toLowerThirdColorBg(k, v) {
  return toLowerThirdColor('overlay.bg');
}

function toLowerThirdColor(type) {
  switch (WS.state['ScoreBoard.Settings.Setting(Overlay.Interactive.LowerThird.Style)']) {
    case 'ColourTeam1':
      return WS.state['ScoreBoard.CurrentGame.Team(1).Color(' + type + ')'];
    case 'ColourTeam2':
      return WS.state['ScoreBoard.CurrentGame.Team(2).Color(' + type + ')'];
    default:
      return '';
  }
}

function toClockType(k, v) {
  'use strict';
  var ret;
  var to = WS.state['ScoreBoard.CurrentGame.TimeoutOwner'];
  var or = WS.state['ScoreBoard.CurrentGame.OfficialReview'];
  var tc = WS.state['ScoreBoard.CurrentGame.Clock(Timeout).Running'];
  var lc = WS.state['ScoreBoard.CurrentGame.Clock(Lineup).Running'];
  var ic = WS.state['ScoreBoard.CurrentGame.Clock(Intermission).Running'];
  var jc = WS.state['ScoreBoard.CurrentGame.InJam'];

  if (tc) {
    ret = WS.state['ScoreBoard.CurrentGame.Clock(Timeout).Name'];
    if (to !== '' && to !== 'O' && or) {
      ret = 'Official Review';
    }
    if (to !== '' && to !== 'O' && !or) {
      ret = 'Team Timeout';
    }
    if (to === 'O') {
      ret = 'Official Timeout';
    }
    $('.ClockDescription').css('backgroundColor', 'red');
  } else if (lc) {
    ret = WS.state['ScoreBoard.CurrentGame.Clock(Lineup).Name'];
    $('.ClockDescription').css('backgroundColor', '#888');
  } else if (ic) {
    var num = WS.state['ScoreBoard.CurrentGame.Clock(Intermission).Number'];
    var max = WS.state['ScoreBoard.CurrentGame.Rule(Period.Number)'];
    var isOfficial = WS.state['ScoreBoard.CurrentGame.OfficialScore'];
    var showDuringOfficial = WS.state['ScoreBoard.CurrentGame.ClockDuringFinalScore'];
    if (isOfficial) {
      if (showDuringOfficial) {
        ret = WS.state['ScoreBoard.Settings.Setting(ScoreBoard.Intermission.OfficialWithClock)'];
      } else {
        ret = WS.state['ScoreBoard.Settings.Setting(ScoreBoard.Intermission.Official)'];
      }
    } else if (num === 0) {
      ret = WS.state['ScoreBoard.Settings.Setting(ScoreBoard.Intermission.PreGame)'];
    } else if (num != max) {
      ret = WS.state['ScoreBoard.Settings.Setting(ScoreBoard.Intermission.Intermission)'];
    } else if (!isOfficial) {
      ret = WS.state['ScoreBoard.Settings.Setting(ScoreBoard.Intermission.Unofficial)'];
    }

    $('.ClockDescription').css('backgroundColor', 'blue');
  } else if (jc) {
    ret = 'Jam';
    $('.ClockDescription').css('backgroundColor', '#888');
  } else {
    ret = 'Coming Up';
    $('.ClockDescription').css('backgroundColor', 'blue');
  }

  return ret;
}
