package com.netsensia.rivalchess.engine.hash

import com.netsensia.rivalchess.consts.BITBOARD_BR
import com.netsensia.rivalchess.consts.BITBOARD_WP
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour

object ZorbristHashCalculator {
    const val START_HASH_VALUE = 1427869295504964227L

    @JvmField
    val pieceHashValues = arrayOf(
            longArrayOf(2757563266877852572L, 6372315729141069125L, 6531282879677255786L, 6217800311556484852L, 4028653356764102261L, 7173945121263241432L, 3140540760890330300L, 186456652255508025L, 4326822066600281601L, 1676275713585176313L, 994786814496869094L, 7266780370156599753L, 1993832195284356489L, 8906581877710625494L, 13143720840670621L, 4898713441463880900L, 2606168633944391863L, 1278377858051695283L, 1882005865304517078L, 3567997896755807464L, 2122005311604240155L, 2319007812912636961L, 4341526477626483922L, 4112961148281161054L, 1873977493594566140L, 305729391848946031L, 1737015787209404385L, 2237900801421661035L, 3194938434899723208L, 6533797044794123108L, 53098511802011562L, 3081966774796706547L, 1696652368246260792L, 2862724276072767459L, 1287517088619610318L, 2602374675812111264L, 4756084200418510689L, 5276272864338057212L, 8687878729112386221L, 4135797612500512402L, 5514543711318410338L, 5560689077408854053L, 2256144847160470449L, 4417513483083909270L, 3035972007832032975L, 7368920397373182932L, 6920167683995549487L, 6247308382891571962L, 5676121683487873905L, 9175320351448874372L, 5910720470467824693L, 907420310966211028L, 6716488881646089174L, 5928558823587778993L, 5915781820118628492L, 39463936643324408L, 5811376027078466953L, 6523428793023374251L, 5956533780815835809L, 3641790615902344529L, 7353877805870994894L, 939029069031068496L, 8125675455391727839L, 4589414129899697073L),
            longArrayOf(706413976754998224L, 5237446959228702633L, 4502343349913478046L, 4284922253622685935L, 794556809418481704L, 6752480058157658297L, 5122029621984886652L, 6882057774524096198L, 6541274272475607484L, 1574242302337288143L, 111900223082258093L, 6631459239802814568L, 526646747656935902L, 3357192273790967373L, 3598688189568699342L, 5546366303086541876L, 1726594687531813407L, 4987647731489328978L, 3580842832605597181L, 7743075973487833223L, 2776180945377874707L, 6742207452261577653L, 3524831375196424107L, 7614586915232374818L, 6115393728617541508L, 1125662212014421892L, 231458207239155535L, 2885178576595681658L, 2690121602411998221L, 3533102710711774971L, 7740701900047567421L, 1255503606430395821L, 8399862489813273836L, 1439234334975020321L, 4763417158037960862L, 4977818468183857538L, 4984644494470203422L, 4351639087268581437L, 2899815003034077612L, 9108461806324676569L, 5172153358648562244L, 2597331834528227009L, 6109954286982553472L, 2912933226757411468L, 7640543903334245893L, 2844971232928040881L, 2279605498837990134L, 3463731465304653534L, 76901528508813291L, 6280005240992472692L, 3260455415836512746L, 4131337061747131489L, 1402852472918327774L, 5102723766602758144L, 2949259100454305372L, 1944378517589392750L, 6606049276124010842L, 4945875818949224017L, 6463697859664636677L, 4563130144202493590L, 1130798484264037885L, 4668778934824795676L, 4808737661328924273L, 3266976802810975809L),
            longArrayOf(3526724629200970276L, 48526046699759754L, 8167123585693394048L, 7126737563418726157L, 3884530645993766909L, 7040664248638881451L, 5837271942644127481L, 3616987892652505963L, 4083408828443113930L, 5348706984051486926L, 7306942631434919894L, 7788911306385470611L, 1920797669047726069L, 5767621404457930932L, 4939777947758921246L, 6828298627902150213L, 4582988639121771633L, 2721189556933358956L, 496370117853838243L, 8143452293799687270L, 6002576524389228294L, 8411950427977459053L, 1823534971628121651L, 3983150712003752658L, 2783514311632729460L, 9094688704651908402L, 1682895755282384617L, 3213042356150788443L, 6174516486991242124L, 5429085076595783743L, 3381254759730602595L, 5479816068083974802L, 8204697593884991320L, 6347530531368040414L, 4490397025128192141L, 8407408328672604196L, 5997021339967186156L, 1947076232270972848L, 6201620772051583256L, 7744342598707609147L, 1939499042085219765L, 6834867949355847005L, 5596041422517147349L, 6345497246432346170L, 1231538645575753339L, 8235430625516224931L, 2583608123937963550L, 6795223553357527787L, 6281788933568863916L, 2216591880958825895L, 1723055813825809089L, 8462832260593510096L, 3096213975196258774L, 7182992208714122364L, 5137375666796102465L, 4641310499159706793L, 8255850789418445260L, 3895969293116932325L, 202121942194941618L, 3001032906906561674L, 4817543641936854517L, 2933098853597619575L, 1729709981808028074L, 2905624112688754045L),
            longArrayOf(4269252335467993961L, 8855045279702453407L, 7395403973528249356L, 4022410470817470542L, 7372074797706027983L, 5872608254904647593L, 5065301752116643350L, 7688499207936066304L, 3218757409228525372L, 4651989491941495962L, 6503996956873938409L, 489241169476201417L, 8609457659388063253L, 2646243653538414237L, 4447266732656982996L, 193915167707836644L, 4529961767168652L, 4737883173481870005L, 6302424584209810931L, 338657460727683711L, 1563300542944841483L, 5327156014778687932L, 2870495655378563278L, 5608507905247097283L, 3590216443108144923L, 2875661783927563348L, 7124359522283911602L, 8428430503568743857L, 4767059299910687386L, 2590273633516971606L, 4761562871388918166L, 2652204195911211977L, 8826896887974165415L, 1065176361428595232L, 9157163046871280177L, 4502759812759664448L, 5726299958902904109L, 530072053618769216L, 895846526316524618L, 467937357922516750L, 8602063605459535850L, 6719205568977338381L, 8583693595904718437L, 4153004816965705917L, 5966796638923899027L, 1032034806207365317L, 1307506363588584492L, 3712735712719939276L, 4343944053518729075L, 1282423760452505319L, 173424631240495461L, 8381780405369056950L, 2199954421593380451L, 444917898291442463L, 6480926845079875269L, 7537216052377744129L, 3774733988261462373L, 7499074026023255496L, 7607843325131718492L, 6093993311717792743L, 2331685763872572587L, 4352876686499200149L, 4235202761664779967L, 5895241459534232072L),
            longArrayOf(5482059440352445727L, 7956339969058211934L, 4954060434318501117L, 8115995729820763036L, 5721347636312841333L, 2551590848249369923L, 8761151027090434770L, 1450048539819847742L, 809275049474077988L, 3913411587935094951L, 7099828467142482864L, 1026992368184896896L, 3925751325938633014L, 9074419762504123157L, 1246437869013631664L, 7378047233887314923L, 474504446116046695L, 681026732928034876L, 7503100584678545241L, 5144675340194602534L, 3281803653846062286L, 4706541021557088044L, 6352787564453770705L, 3307736953582204212L, 925057422725221765L, 4328643808080894957L, 1442583851450410906L, 437666655235918844L, 7750516601172680244L, 7066318828270012948L, 8430491571929385137L, 9058449950227869823L, 5757510486901178080L, 7279843450518321304L, 43127866548369872L, 7801665669407941225L, 869472628767054139L, 4455912880072542546L, 4776599164076577267L, 4995651884202018016L, 9205339851548284274L, 2991217360619359484L, 8113533920899574669L, 2725531378807444516L, 1903209836595839133L, 869792416915504714L, 2624383920180508576L, 7463622451271065457L, 4031369777553205636L, 8198744696199806783L, 6209155299517931176L, 7716991479317077036L, 8552324997505406292L, 5190792594819684254L, 5405583483881399091L, 1412560298191341700L, 103316124955288142L, 2570802615516381813L, 2742678712837978301L, 4736800798767083968L, 4689925640499760316L, 2011713206560716592L, 7698839976705558703L, 2101693932821614576L),
            longArrayOf(9134090328332492302L, 5263125285456236617L, 2002707346266872506L, 8457109776747425942L, 6277262185843779334L, 1261513154258218264L, 743595506557340024L, 5118696197521219182L, 6389320403900203415L, 4792717765137656718L, 4590599381035109524L, 4633000749212460643L, 8853302209228134548L, 829933116792773481L, 7454206074684803970L, 1331564509239791452L, 2434809458655800162L, 7528690117223614777L, 7112175028828574739L, 889991979395197613L, 3510095911311826305L, 6167459476425399399L, 8472822513950460484L, 878660785199353768L, 4718343879738261675L, 5624193844673635964L, 5799899023464378991L, 1093009618003491558L, 4513119637856023694L, 2754385592750650280L, 3524340711222428242L, 1204651937704060576L, 2768034571782041027L, 5845313009030790673L, 2379638552628418965L, 705221492190164429L, 5692332855001729224L, 5134048240937163093L, 7862354069605444682L, 4652522373255046423L, 6562611794933572479L, 7225846535560570563L, 6798374679349098073L, 1923114751136460212L, 440770511502372502L, 4134483883707043431L, 2506428789614300520L, 3546362436556191123L, 3497886265811279489L, 932522976137894852L, 5443410947940562661L, 7395179419574312861L, 1925597547461550297L, 3083566288934567853L, 6391724655495198176L, 8164844593195065182L, 2909857915875545973L, 1053375534840206791L, 1296244821721594611L, 263126646077365181L, 3777884168630782348L, 1152166463363885612L, 9217013729487695673L, 1775948033234846362L),
            longArrayOf(5691615292360083080L, 762190898621373795L, 5821639506728817460L, 1738661259230741628L, 3019431854270606861L, 83552056897441975L, 6609846280203152870L, 435371541937689352L, 2282570952197666100L, 595876304234693565L, 9147403099394789001L, 7533035522525488026L, 5691934491529333253L, 2980086244887624376L, 8013905283156048390L, 363527508688688673L, 7374259252667187068L, 362681824165828228L, 4847910485499809639L, 2253055107480512507L, 111529475954971157L, 4278544012352367756L, 2766192083051374023L, 7516265602420957726L, 5014460317765925511L, 659359411695663573L, 5386737622117173576L, 1038915006615490181L, 7919396185844987858L, 1172731710242206002L, 7494156588627708487L, 6160424331092024832L, 4326581697273687236L, 7014216158415520524L, 5574249326673472790L, 1730768916092988523L, 1445837622325011940L, 3821127040008388125L, 1111961441971770546L, 1611143702751896527L, 6396075583328649777L, 7218836424211858955L, 1793189800360944425L, 7339153043257517224L, 6738837834036705714L, 3372066062997567342L, 7042377330485208733L, 5844450985688756751L, 3227307292985264729L, 5166434995508659230L, 4407216685436238279L, 785947468814544316L, 5494797069623248625L, 9064334486126848534L, 862322936918398010L, 5073869142572136099L, 14625017500494987L, 6176046267152986925L, 2976819429600612209L, 4310591706489711250L, 6681320062885496862L, 8989889258273935260L, 3722163576311367700L, 1805927480862668597L),
            longArrayOf(1403565454462916255L, 9218273974760043426L, 6983677847517772240L, 7331591968540275878L, 4094079603891112668L, 3645458482569787123L, 5885980300442319723L, 3876507597005764248L, 6234295781416942034L, 3610081461213122612L, 1773637784319159881L, 7172009397515594175L, 8272488767347050677L, 3757391311643631956L, 8704162202920884023L, 8839713473012409630L, 3305289356052602520L, 4395246859341533893L, 4853354755476472394L, 6088014623487808889L, 8527577002770203673L, 4619286165257909464L, 5197689722597111352L, 1424875007737058133L, 1071196320061364944L, 8209690818139668736L, 3965369329144655185L, 5944407338050120738L, 1061778754555020954L, 7886209233372012600L, 5092299202595764466L, 7140620755769072024L, 8826446383409644231L, 2139401308830923225L, 6628160763980947324L, 1289382631800531686L, 4910706469776067051L, 6714723210355708126L, 6576358097832606283L, 2087337905894618112L, 2228344893847779585L, 5204208396506408030L, 8019687633900796640L, 2927389921365747343L, 6979770986371552658L, 710955057447159835L, 1246835428103661368L, 4288239899209224309L, 414649854433031187L, 3098469592891386876L, 5644636714708657522L, 8397734769590269397L, 7561745469996983081L, 6916035008389787065L, 6700392039785659203L, 8055163598027508892L, 3826991975611290873L, 4673557597793689875L, 7034777241825369919L, 4770906406929878243L, 1061103761288165190L, 7135194294936601439L, 7406295689631352094L, 1113272196100689149L),
            longArrayOf(1966450248190379088L, 2993865635034902059L, 8788203921705410991L, 9168583593820377450L, 1237464767808744413L, 1776894105739964056L, 5473776405481206593L, 3108517860177137431L, 5682329741821421590L, 9183530200379101140L, 3797186426917148354L, 1931394037295312024L, 2148957555106458413L, 8223722867305992927L, 1527282297679490533L, 5686607043513838725L, 3119975448559538420L, 8971725973451123922L, 4670712296639275156L, 7952526744630924527L, 8112254140019974140L, 1098180819886001975L, 2195958098201965283L, 146480965931442822L, 1000133041828712324L, 8859715321010272914L, 7713927785108031934L, 4664478895417600120L, 323237241289302876L, 2029739124449746224L, 5314045977537512672L, 7761665812734607116L, 7389606616456274226L, 5853730557960210518L, 6405457160064940081L, 7643336737387839820L, 801209189917229092L, 4227962273868069596L, 3067822289957023955L, 133447937448989907L, 7326153904809858610L, 3347788873011704914L, 2542198793675049436L, 3933137803842118338L, 4740396057898551661L, 8202669349863677922L, 7840482582246345144L, 1176356952632570992L, 5769590063640359052L, 8782901109905408165L, 329571181494047202L, 3879829922156060555L, 962783806119609348L, 6701909345750326547L, 1879670639718369096L, 6664022440892888548L, 5701413165223646902L, 418569841824620243L, 3586682514319447750L, 1154800286085849790L, 3158280596792866687L, 3691038129842476001L, 3264887083481036557L, 8923601683697323768L),
            longArrayOf(4978977443718634328L, 7288401877211219539L, 507448495946020687L, 261342561650965313L, 9103401774020839501L, 2811272625307151485L, 6136143827755009479L, 2513761986502584347L, 2618037343322452944L, 5139270459733357277L, 6838266761075862697L, 5792000861464993307L, 797495321118367286L, 1689440298187033107L, 7742957107729489807L, 108982139420953125L, 1368770425759240784L, 8975818365220101167L, 5171348940803071326L, 7429096152147114621L, 8439122944226245896L, 8969861888678684724L, 4496306846960821643L, 1249649415168978015L, 7224215098383632731L, 7234204955776561756L, 1908939853186866432L, 4613046688406647723L, 979409278753174233L, 5314833019430831516L, 6320929885571283525L, 4506518407239481613L, 9191767324412116007L, 534728121186412623L, 4589263512943122260L, 7595055229592911986L, 4136038448724948633L, 666042454987324494L, 559356573438353282L, 2497096404016838989L, 876772448588838374L, 2334172993029750454L, 478558549750751171L, 5692644283346631657L, 1608177764376920769L, 8203657446035958755L, 3560658139861536498L, 7969854509332728097L, 766225182024402454L, 7230810442083413000L, 3409645618456651471L, 8747460773660686810L, 1156376364374428434L, 259907978959573025L, 1134256150512529358L, 9191467588711542031L, 3144543534942117624L, 4615473031590185602L, 1021874242214398643L, 507301541282945445L, 3340389840876346908L, 814010765659554563L, 8230058377636026554L, 8338979323633225138L),
            longArrayOf(1918666253752120829L, 5123785239787297170L, 8260467834230340948L, 720956868684585182L, 2625801173904837038L, 3442049256491471718L, 2924963729754111467L, 2932143463216750042L, 4141774046760525340L, 7124333084796562409L, 4583803384565297833L, 540105885968327263L, 6534998781351102301L, 7603388969104123373L, 1357367707300989722L, 451936749081903360L, 360461024317963340L, 5525004701282371602L, 8202188898962120645L, 2226657532892967402L, 4613010720567674697L, 7359905980603219269L, 6826810207863871489L, 3894350494685017003L, 6695962586797368461L, 8016002439923018986L, 7917644687140354836L, 7843954652503033120L, 6483045143331120470L, 3771306383775362797L, 8623750330209127396L, 2900091261158332736L, 5663671944283168781L, 7446022214448060918L, 3567290127817993180L, 6013836022251678654L, 489903320583615529L, 2213753398843631372L, 785546206712758176L, 8097177210502977517L, 6939208963085844221L, 6469328468874844424L, 672985058695375740L, 4161058687295838148L, 7941065023635809346L, 6151778816897791754L, 9013150994096788929L, 2576155270622500110L, 5189079453494188777L, 7178303791130062751L, 4730696639737107366L, 6497451371231361292L, 3305587776220532337L, 5299835837372005486L, 4399911704324168620L, 4565360990101716732L, 7837932032562291850L, 7356609075281478700L, 2671276588638382746L, 4284991231858828143L, 8609346109930705370L, 3737809786861683657L, 7460576325325589108L, 76027739059752509L),
            longArrayOf(9208188818834422943L, 5079652584320734989L, 5625753500461165109L, 2522764131739476098L, 922397543745308399L, 1849530567864289883L, 3565871539435181739L, 8501945789448278742L, 2834020835440806116L, 2114898699739419510L, 8156849327267637025L, 7417872299570049773L, 760442579520793625L, 8125352974898441086L, 7372747514993796401L, 4820007705677169948L, 5094849792765808884L, 8654594184840000990L, 1043002158891838519L, 30069737146852339L, 7480840281579044543L, 5617509422479735054L, 8087298089913277531L, 2732971805427057233L, 2139833233488709521L, 4021782638741383288L, 8661487598710505540L, 4811833499348751086L, 6129950681643071213L, 4462470102654327859L, 5370869476704701078L, 2247623128255100219L, 6095943374180634947L, 4932188522700420147L, 4913325323529309583L, 3493168178410901252L, 3408574234029055147L, 1781269879077519466L, 6009011768553083766L, 7359460891617983503L, 4078903063976677065L, 1891844119477713966L, 3111342753299638506L, 8498343264139653994L, 8554521356288376209L, 8647006971813776110L, 1405867315492784046L, 3261249132213245190L, 4172321625119480057L, 1422367203506234086L, 6260367524173538434L, 608749605062299397L, 2045889148659716268L, 2122806103854230142L, 8839124020569954204L, 8418931016733783342L, 5466714312773596092L, 2059608657860469959L, 1622528598239002687L, 228144609206946361L, 5588235677104977126L, 3157823198315106642L, 1729483203816602699L, 3116580019810902115L)
    )

    private val moverHashValues = longArrayOf(6612194290785701391L, 7796428774704130372L)

    @JvmStatic
    fun calculateHash(engineBoard: EngineBoard): Long {
        var hashValue = START_HASH_VALUE
        for (bitNum in 0..63) {
            for (piece in BITBOARD_WP..BITBOARD_BR) {
                if (engineBoard.getBitboard(piece) and (1L shl bitNum) != 0L) {
                    hashValue = hashValue xor pieceHashValues[piece][bitNum]
                }
            }
        }
        hashValue = hashValue xor moverHashValues[if (engineBoard.mover == Colour.WHITE) 0 else 1]
        return hashValue
    }

    @JvmStatic
    val whiteMoverHashValue: Long
        get() = moverHashValues[0]

    @JvmStatic
    val blackMoverHashValue: Long
        get() = moverHashValues[1]
}