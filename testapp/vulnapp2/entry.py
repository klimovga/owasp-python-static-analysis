#import index
#import html.profile
#import html.doeditnews
#import html.statistics
#import html.files
#import html.logout
#import html.dodeletenews
#import html.news
#import html.register
#import html.calculator
#import html.viewprofile
#import html.editnews
#import html.login
#import html.index
#import html.deletefiles
#import html.addfiles
#import html.deletenews
#import html.addnews
#import html.showfile

import MySQLdb
import html.settings
import mp_request

# non-deterministically call modules

#random = raw_input()
req = mp_request.args
session_id = req.session_id
con = MySQLdb.connect(  host = settings.database_settings["host"],
    user = settings.database_settings["login"],
    passwd = settings.database_settings["password"],
    db = settings.database_settings["database"])
cur = con.cursor()
query = """SELECT user_id FROM sessions WHERE session_id='%s' AND expire_time > now()""" % (session_id,)
cur.execute(query)

args = []
#if random == 'index':
#    index.handler(req, args)
#elif random == 'profile':
#    profile.handler(req, args)
#elif random == 'doeditnews':
#    doeditnews.handler(req, args)
#elif random == 'statistics':
#    statistics.handler(req, args)
#elif random == 'files':
#    files.handler(req, args)
#elif random == 'logout':
#    logout.handler(req, args)
#elif random == 'dodeletenews':
#    dodeletenews.handler(req, args)
#elif random == 'news':
#    news.handler(req, args)
#elif random == 'register':
#    register.handler(req, args)
#elif random == 'calculator':
#    calculator.handler(req, args)
#elif random == 'viewprofile':
#    viewprofile.handler(req, args)
#elif random == 'editnews':
#    editnews.handler(req, args)
#elif random == 'login':
#    login.handler(req, args)
#elif random == 'index':
#    index.handler(req, args)
#elif random == 'deletefiles':
#    deletefiles.handler(req, args)
#elif random == 'addfiles':
#    addfiles.handler(req, args)
#elif random == 'deletenews':
#    deletenews.handler(req, args)
#elif random == 'addnews':
#    addnews.handler(req, args)
#elif random == 'showfile':
#    showfile.handler(req, args)
