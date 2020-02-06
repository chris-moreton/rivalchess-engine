package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.bitboards.MagicBitboards;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.model.Board;

import java.util.List;

public final class EngineChessBoard {
    public static final String START_POS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Deprecated
    public int whitePieceValues = 0;
    @Deprecated
    public int blackPieceValues = 0;
    @Deprecated
    public int whitePawnValues = 0;
    @Deprecated
    public int blackPawnValues = 0;

    public final int[] pieceSquareValues = new int[12];
    public final int[] pieceSquareValuesEndGame = new int[12];

    public long[] pieceBitboards;

    public int m_castlePrivileges;
    public boolean m_isWhiteToMove;
    public byte m_whiteKingSquare;
    public byte m_blackKingSquare;
    public int m_movesMade;

    @Deprecated
    public final byte[] squareContents = new byte[64];

    public boolean m_isOnNullMove = false;

    protected int[] m_legalMoves;
    protected int m_numLegalMoves;

    public MoveDetail[] m_moveList;

    public int m_halfMoveCount = 0;

    private final static long[][] m_pieceHashValues =
            {
                    {2757563266877852572L, 6372315729141069125L, 6531282879677255786L, 6217800311556484852L, 4028653356764102261L, 7173945121263241432L, 3140540760890330300L, 186456652255508025L, 4326822066600281601L, 1676275713585176313L, 994786814496869094L, 7266780370156599753L, 1993832195284356489L, 8906581877710625494L, 13143720840670621L, 4898713441463880900L, 2606168633944391863L, 1278377858051695283L, 1882005865304517078L, 3567997896755807464L, 2122005311604240155L, 2319007812912636961L, 4341526477626483922L, 4112961148281161054L, 1873977493594566140L, 305729391848946031L, 1737015787209404385L, 2237900801421661035L, 3194938434899723208L, 6533797044794123108L, 53098511802011562L, 3081966774796706547L, 1696652368246260792L, 2862724276072767459L, 1287517088619610318L, 2602374675812111264L, 4756084200418510689L, 5276272864338057212L, 8687878729112386221L, 4135797612500512402L, 5514543711318410338L, 5560689077408854053L, 2256144847160470449L, 4417513483083909270L, 3035972007832032975L, 7368920397373182932L, 6920167683995549487L, 6247308382891571962L, 5676121683487873905L, 9175320351448874372L, 5910720470467824693L, 907420310966211028L, 6716488881646089174L, 5928558823587778993L, 5915781820118628492L, 39463936643324408L, 5811376027078466953L, 6523428793023374251L, 5956533780815835809L, 3641790615902344529L, 7353877805870994894L, 939029069031068496L, 8125675455391727839L, 4589414129899697073L,},
                    {706413976754998224L, 5237446959228702633L, 4502343349913478046L, 4284922253622685935L, 794556809418481704L, 6752480058157658297L, 5122029621984886652L, 6882057774524096198L, 6541274272475607484L, 1574242302337288143L, 111900223082258093L, 6631459239802814568L, 526646747656935902L, 3357192273790967373L, 3598688189568699342L, 5546366303086541876L, 1726594687531813407L, 4987647731489328978L, 3580842832605597181L, 7743075973487833223L, 2776180945377874707L, 6742207452261577653L, 3524831375196424107L, 7614586915232374818L, 6115393728617541508L, 1125662212014421892L, 231458207239155535L, 2885178576595681658L, 2690121602411998221L, 3533102710711774971L, 7740701900047567421L, 1255503606430395821L, 8399862489813273836L, 1439234334975020321L, 4763417158037960862L, 4977818468183857538L, 4984644494470203422L, 4351639087268581437L, 2899815003034077612L, 9108461806324676569L, 5172153358648562244L, 2597331834528227009L, 6109954286982553472L, 2912933226757411468L, 7640543903334245893L, 2844971232928040881L, 2279605498837990134L, 3463731465304653534L, 76901528508813291L, 6280005240992472692L, 3260455415836512746L, 4131337061747131489L, 1402852472918327774L, 5102723766602758144L, 2949259100454305372L, 1944378517589392750L, 6606049276124010842L, 4945875818949224017L, 6463697859664636677L, 4563130144202493590L, 1130798484264037885L, 4668778934824795676L, 4808737661328924273L, 3266976802810975809L,},
                    {3526724629200970276L, 48526046699759754L, 8167123585693394048L, 7126737563418726157L, 3884530645993766909L, 7040664248638881451L, 5837271942644127481L, 3616987892652505963L, 4083408828443113930L, 5348706984051486926L, 7306942631434919894L, 7788911306385470611L, 1920797669047726069L, 5767621404457930932L, 4939777947758921246L, 6828298627902150213L, 4582988639121771633L, 2721189556933358956L, 496370117853838243L, 8143452293799687270L, 6002576524389228294L, 8411950427977459053L, 1823534971628121651L, 3983150712003752658L, 2783514311632729460L, 9094688704651908402L, 1682895755282384617L, 3213042356150788443L, 6174516486991242124L, 5429085076595783743L, 3381254759730602595L, 5479816068083974802L, 8204697593884991320L, 6347530531368040414L, 4490397025128192141L, 8407408328672604196L, 5997021339967186156L, 1947076232270972848L, 6201620772051583256L, 7744342598707609147L, 1939499042085219765L, 6834867949355847005L, 5596041422517147349L, 6345497246432346170L, 1231538645575753339L, 8235430625516224931L, 2583608123937963550L, 6795223553357527787L, 6281788933568863916L, 2216591880958825895L, 1723055813825809089L, 8462832260593510096L, 3096213975196258774L, 7182992208714122364L, 5137375666796102465L, 4641310499159706793L, 8255850789418445260L, 3895969293116932325L, 202121942194941618L, 3001032906906561674L, 4817543641936854517L, 2933098853597619575L, 1729709981808028074L, 2905624112688754045L,},
                    {4269252335467993961L, 8855045279702453407L, 7395403973528249356L, 4022410470817470542L, 7372074797706027983L, 5872608254904647593L, 5065301752116643350L, 7688499207936066304L, 3218757409228525372L, 4651989491941495962L, 6503996956873938409L, 489241169476201417L, 8609457659388063253L, 2646243653538414237L, 4447266732656982996L, 193915167707836644L, 4529961767168652L, 4737883173481870005L, 6302424584209810931L, 338657460727683711L, 1563300542944841483L, 5327156014778687932L, 2870495655378563278L, 5608507905247097283L, 3590216443108144923L, 2875661783927563348L, 7124359522283911602L, 8428430503568743857L, 4767059299910687386L, 2590273633516971606L, 4761562871388918166L, 2652204195911211977L, 8826896887974165415L, 1065176361428595232L, 9157163046871280177L, 4502759812759664448L, 5726299958902904109L, 530072053618769216L, 895846526316524618L, 467937357922516750L, 8602063605459535850L, 6719205568977338381L, 8583693595904718437L, 4153004816965705917L, 5966796638923899027L, 1032034806207365317L, 1307506363588584492L, 3712735712719939276L, 4343944053518729075L, 1282423760452505319L, 173424631240495461L, 8381780405369056950L, 2199954421593380451L, 444917898291442463L, 6480926845079875269L, 7537216052377744129L, 3774733988261462373L, 7499074026023255496L, 7607843325131718492L, 6093993311717792743L, 2331685763872572587L, 4352876686499200149L, 4235202761664779967L, 5895241459534232072L,},
                    {5482059440352445727L, 7956339969058211934L, 4954060434318501117L, 8115995729820763036L, 5721347636312841333L, 2551590848249369923L, 8761151027090434770L, 1450048539819847742L, 809275049474077988L, 3913411587935094951L, 7099828467142482864L, 1026992368184896896L, 3925751325938633014L, 9074419762504123157L, 1246437869013631664L, 7378047233887314923L, 474504446116046695L, 681026732928034876L, 7503100584678545241L, 5144675340194602534L, 3281803653846062286L, 4706541021557088044L, 6352787564453770705L, 3307736953582204212L, 925057422725221765L, 4328643808080894957L, 1442583851450410906L, 437666655235918844L, 7750516601172680244L, 7066318828270012948L, 8430491571929385137L, 9058449950227869823L, 5757510486901178080L, 7279843450518321304L, 43127866548369872L, 7801665669407941225L, 869472628767054139L, 4455912880072542546L, 4776599164076577267L, 4995651884202018016L, 9205339851548284274L, 2991217360619359484L, 8113533920899574669L, 2725531378807444516L, 1903209836595839133L, 869792416915504714L, 2624383920180508576L, 7463622451271065457L, 4031369777553205636L, 8198744696199806783L, 6209155299517931176L, 7716991479317077036L, 8552324997505406292L, 5190792594819684254L, 5405583483881399091L, 1412560298191341700L, 103316124955288142L, 2570802615516381813L, 2742678712837978301L, 4736800798767083968L, 4689925640499760316L, 2011713206560716592L, 7698839976705558703L, 2101693932821614576L,},
                    {9134090328332492302L, 5263125285456236617L, 2002707346266872506L, 8457109776747425942L, 6277262185843779334L, 1261513154258218264L, 743595506557340024L, 5118696197521219182L, 6389320403900203415L, 4792717765137656718L, 4590599381035109524L, 4633000749212460643L, 8853302209228134548L, 829933116792773481L, 7454206074684803970L, 1331564509239791452L, 2434809458655800162L, 7528690117223614777L, 7112175028828574739L, 889991979395197613L, 3510095911311826305L, 6167459476425399399L, 8472822513950460484L, 878660785199353768L, 4718343879738261675L, 5624193844673635964L, 5799899023464378991L, 1093009618003491558L, 4513119637856023694L, 2754385592750650280L, 3524340711222428242L, 1204651937704060576L, 2768034571782041027L, 5845313009030790673L, 2379638552628418965L, 705221492190164429L, 5692332855001729224L, 5134048240937163093L, 7862354069605444682L, 4652522373255046423L, 6562611794933572479L, 7225846535560570563L, 6798374679349098073L, 1923114751136460212L, 440770511502372502L, 4134483883707043431L, 2506428789614300520L, 3546362436556191123L, 3497886265811279489L, 932522976137894852L, 5443410947940562661L, 7395179419574312861L, 1925597547461550297L, 3083566288934567853L, 6391724655495198176L, 8164844593195065182L, 2909857915875545973L, 1053375534840206791L, 1296244821721594611L, 263126646077365181L, 3777884168630782348L, 1152166463363885612L, 9217013729487695673L, 1775948033234846362L,},
                    {5691615292360083080L, 762190898621373795L, 5821639506728817460L, 1738661259230741628L, 3019431854270606861L, 83552056897441975L, 6609846280203152870L, 435371541937689352L, 2282570952197666100L, 595876304234693565L, 9147403099394789001L, 7533035522525488026L, 5691934491529333253L, 2980086244887624376L, 8013905283156048390L, 363527508688688673L, 7374259252667187068L, 362681824165828228L, 4847910485499809639L, 2253055107480512507L, 111529475954971157L, 4278544012352367756L, 2766192083051374023L, 7516265602420957726L, 5014460317765925511L, 659359411695663573L, 5386737622117173576L, 1038915006615490181L, 7919396185844987858L, 1172731710242206002L, 7494156588627708487L, 6160424331092024832L, 4326581697273687236L, 7014216158415520524L, 5574249326673472790L, 1730768916092988523L, 1445837622325011940L, 3821127040008388125L, 1111961441971770546L, 1611143702751896527L, 6396075583328649777L, 7218836424211858955L, 1793189800360944425L, 7339153043257517224L, 6738837834036705714L, 3372066062997567342L, 7042377330485208733L, 5844450985688756751L, 3227307292985264729L, 5166434995508659230L, 4407216685436238279L, 785947468814544316L, 5494797069623248625L, 9064334486126848534L, 862322936918398010L, 5073869142572136099L, 14625017500494987L, 6176046267152986925L, 2976819429600612209L, 4310591706489711250L, 6681320062885496862L, 8989889258273935260L, 3722163576311367700L, 1805927480862668597L,},
                    {1403565454462916255L, 9218273974760043426L, 6983677847517772240L, 7331591968540275878L, 4094079603891112668L, 3645458482569787123L, 5885980300442319723L, 3876507597005764248L, 6234295781416942034L, 3610081461213122612L, 1773637784319159881L, 7172009397515594175L, 8272488767347050677L, 3757391311643631956L, 8704162202920884023L, 8839713473012409630L, 3305289356052602520L, 4395246859341533893L, 4853354755476472394L, 6088014623487808889L, 8527577002770203673L, 4619286165257909464L, 5197689722597111352L, 1424875007737058133L, 1071196320061364944L, 8209690818139668736L, 3965369329144655185L, 5944407338050120738L, 1061778754555020954L, 7886209233372012600L, 5092299202595764466L, 7140620755769072024L, 8826446383409644231L, 2139401308830923225L, 6628160763980947324L, 1289382631800531686L, 4910706469776067051L, 6714723210355708126L, 6576358097832606283L, 2087337905894618112L, 2228344893847779585L, 5204208396506408030L, 8019687633900796640L, 2927389921365747343L, 6979770986371552658L, 710955057447159835L, 1246835428103661368L, 4288239899209224309L, 414649854433031187L, 3098469592891386876L, 5644636714708657522L, 8397734769590269397L, 7561745469996983081L, 6916035008389787065L, 6700392039785659203L, 8055163598027508892L, 3826991975611290873L, 4673557597793689875L, 7034777241825369919L, 4770906406929878243L, 1061103761288165190L, 7135194294936601439L, 7406295689631352094L, 1113272196100689149L,},
                    {1966450248190379088L, 2993865635034902059L, 8788203921705410991L, 9168583593820377450L, 1237464767808744413L, 1776894105739964056L, 5473776405481206593L, 3108517860177137431L, 5682329741821421590L, 9183530200379101140L, 3797186426917148354L, 1931394037295312024L, 2148957555106458413L, 8223722867305992927L, 1527282297679490533L, 5686607043513838725L, 3119975448559538420L, 8971725973451123922L, 4670712296639275156L, 7952526744630924527L, 8112254140019974140L, 1098180819886001975L, 2195958098201965283L, 146480965931442822L, 1000133041828712324L, 8859715321010272914L, 7713927785108031934L, 4664478895417600120L, 323237241289302876L, 2029739124449746224L, 5314045977537512672L, 7761665812734607116L, 7389606616456274226L, 5853730557960210518L, 6405457160064940081L, 7643336737387839820L, 801209189917229092L, 4227962273868069596L, 3067822289957023955L, 133447937448989907L, 7326153904809858610L, 3347788873011704914L, 2542198793675049436L, 3933137803842118338L, 4740396057898551661L, 8202669349863677922L, 7840482582246345144L, 1176356952632570992L, 5769590063640359052L, 8782901109905408165L, 329571181494047202L, 3879829922156060555L, 962783806119609348L, 6701909345750326547L, 1879670639718369096L, 6664022440892888548L, 5701413165223646902L, 418569841824620243L, 3586682514319447750L, 1154800286085849790L, 3158280596792866687L, 3691038129842476001L, 3264887083481036557L, 8923601683697323768L,},
                    {4978977443718634328L, 7288401877211219539L, 507448495946020687L, 261342561650965313L, 9103401774020839501L, 2811272625307151485L, 6136143827755009479L, 2513761986502584347L, 2618037343322452944L, 5139270459733357277L, 6838266761075862697L, 5792000861464993307L, 797495321118367286L, 1689440298187033107L, 7742957107729489807L, 108982139420953125L, 1368770425759240784L, 8975818365220101167L, 5171348940803071326L, 7429096152147114621L, 8439122944226245896L, 8969861888678684724L, 4496306846960821643L, 1249649415168978015L, 7224215098383632731L, 7234204955776561756L, 1908939853186866432L, 4613046688406647723L, 979409278753174233L, 5314833019430831516L, 6320929885571283525L, 4506518407239481613L, 9191767324412116007L, 534728121186412623L, 4589263512943122260L, 7595055229592911986L, 4136038448724948633L, 666042454987324494L, 559356573438353282L, 2497096404016838989L, 876772448588838374L, 2334172993029750454L, 478558549750751171L, 5692644283346631657L, 1608177764376920769L, 8203657446035958755L, 3560658139861536498L, 7969854509332728097L, 766225182024402454L, 7230810442083413000L, 3409645618456651471L, 8747460773660686810L, 1156376364374428434L, 259907978959573025L, 1134256150512529358L, 9191467588711542031L, 3144543534942117624L, 4615473031590185602L, 1021874242214398643L, 507301541282945445L, 3340389840876346908L, 814010765659554563L, 8230058377636026554L, 8338979323633225138L,},
                    {1918666253752120829L, 5123785239787297170L, 8260467834230340948L, 720956868684585182L, 2625801173904837038L, 3442049256491471718L, 2924963729754111467L, 2932143463216750042L, 4141774046760525340L, 7124333084796562409L, 4583803384565297833L, 540105885968327263L, 6534998781351102301L, 7603388969104123373L, 1357367707300989722L, 451936749081903360L, 360461024317963340L, 5525004701282371602L, 8202188898962120645L, 2226657532892967402L, 4613010720567674697L, 7359905980603219269L, 6826810207863871489L, 3894350494685017003L, 6695962586797368461L, 8016002439923018986L, 7917644687140354836L, 7843954652503033120L, 6483045143331120470L, 3771306383775362797L, 8623750330209127396L, 2900091261158332736L, 5663671944283168781L, 7446022214448060918L, 3567290127817993180L, 6013836022251678654L, 489903320583615529L, 2213753398843631372L, 785546206712758176L, 8097177210502977517L, 6939208963085844221L, 6469328468874844424L, 672985058695375740L, 4161058687295838148L, 7941065023635809346L, 6151778816897791754L, 9013150994096788929L, 2576155270622500110L, 5189079453494188777L, 7178303791130062751L, 4730696639737107366L, 6497451371231361292L, 3305587776220532337L, 5299835837372005486L, 4399911704324168620L, 4565360990101716732L, 7837932032562291850L, 7356609075281478700L, 2671276588638382746L, 4284991231858828143L, 8609346109930705370L, 3737809786861683657L, 7460576325325589108L, 76027739059752509L,},
                    {9208188818834422943L, 5079652584320734989L, 5625753500461165109L, 2522764131739476098L, 922397543745308399L, 1849530567864289883L, 3565871539435181739L, 8501945789448278742L, 2834020835440806116L, 2114898699739419510L, 8156849327267637025L, 7417872299570049773L, 760442579520793625L, 8125352974898441086L, 7372747514993796401L, 4820007705677169948L, 5094849792765808884L, 8654594184840000990L, 1043002158891838519L, 30069737146852339L, 7480840281579044543L, 5617509422479735054L, 8087298089913277531L, 2732971805427057233L, 2139833233488709521L, 4021782638741383288L, 8661487598710505540L, 4811833499348751086L, 6129950681643071213L, 4462470102654327859L, 5370869476704701078L, 2247623128255100219L, 6095943374180634947L, 4932188522700420147L, 4913325323529309583L, 3493168178410901252L, 3408574234029055147L, 1781269879077519466L, 6009011768553083766L, 7359460891617983503L, 4078903063976677065L, 1891844119477713966L, 3111342753299638506L, 8498343264139653994L, 8554521356288376209L, 8647006971813776110L, 1405867315492784046L, 3261249132213245190L, 4172321625119480057L, 1422367203506234086L, 6260367524173538434L, 608749605062299397L, 2045889148659716268L, 2122806103854230142L, 8839124020569954204L, 8418931016733783342L, 5466714312773596092L, 2059608657860469959L, 1622528598239002687L, 228144609206946361L, 5588235677104977126L, 3157823198315106642L, 1729483203816602699L, 3116580019810902115L}
            };

    private final static long[] m_moverHashValues = {6612194290785701391L, 7796428774704130372L};
    private final static long m_hashSwitchMovers = m_moverHashValues[RivalConstants.WHITE] ^ m_moverHashValues[RivalConstants.BLACK];

    private static final long START_HASH_VALUE = 1427869295504964227L;
    private static final long START_PAWN_HASH_VALUE = 5454534288458826522L;

    private long hashValue;
    private long pawnHashValue;

    public EngineChessBoard() {
        initArrays();
    }

    public void setBoard(Board board) {
        this.m_movesMade = 0;
        this.m_halfMoveCount = board.getHalfMoveCount();
        setBitboards(board);
        initHash();
    }

    private void initArrays() {
        int size = RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES;
        this.m_moveList = new MoveDetail[size];
        for (int i = 0; i < size; i++) {
            this.m_moveList[i] = new MoveDetail();
        }
    }

    private void initHash() {
        this.hashValue = START_HASH_VALUE;
        this.pawnHashValue = START_PAWN_HASH_VALUE;

        for (int bitNum = 0; bitNum < 64; bitNum++) {
            for (int piece = RivalConstants.WP; piece <= RivalConstants.BR; piece++) {
                if ((this.pieceBitboards[piece] & (1L << bitNum)) != 0) {
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[piece][bitNum];
                    if (piece == RivalConstants.WP || piece == RivalConstants.BP) {
                        this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[piece][bitNum];
                    }
                }
            }
        }

        this.hashValue ^= EngineChessBoard.m_moverHashValues[m_isWhiteToMove ? RivalConstants.WHITE : RivalConstants.BLACK];
    }

    public long getPawnHashValue() {
        return pawnHashValue;
    }

    public long getHashValue() {
        return hashValue;
    }

    public SquareOccupant getSquareOccupant(int bitRef) {
        return SquareOccupant.fromIndex(squareContents[bitRef]);
    }

    public Piece getPiece(int bitRef) {
        switch (squareContents[bitRef]) {
            case RivalConstants.WP:
            case RivalConstants.BP:
                 return Piece.PAWN;
            case RivalConstants.WB:
            case RivalConstants.BB:
                return Piece.BISHOP;
            case RivalConstants.WN:
            case RivalConstants.BN:
                return Piece.KNIGHT;
            case RivalConstants.WR:
            case RivalConstants.BR:
                return Piece.ROOK;
            case RivalConstants.WQ:
            case RivalConstants.BQ:
                return Piece.QUEEN;
            case RivalConstants.WK:
            case RivalConstants.BK:
                return Piece.KING;
            default:
                return Piece.NONE;
        }
    }

    public boolean isGameOver() {
        generateLegalMoves();

        for (int i=0; i<m_numLegalMoves; i++) {
            if (isMoveLegal(m_legalMoves[i])) {
                return false;
            }
        }

        return true;
    }

    public boolean isNonMoverInCheck() {
        return m_isWhiteToMove ?
                isSquareAttacked(m_blackKingSquare, true) :
                isSquareAttacked(m_whiteKingSquare, false);
    }

    public boolean isCheck() {
        return m_isWhiteToMove ?
                isSquareAttacked(m_whiteKingSquare, false) :
                isSquareAttacked(m_blackKingSquare, true);
    }

    public boolean isSquareAttacked(final int attackedSquare, final boolean isWhiteAttacking) {
        if (isWhiteAttacking) {
            if ((pieceBitboards[RivalConstants.WN] & Bitboards.knightMoves.get(attackedSquare)) != 0 ||
                    (pieceBitboards[RivalConstants.WK] & Bitboards.kingMoves.get(attackedSquare)) != 0 ||
                    (pieceBitboards[RivalConstants.WP] & Bitboards.blackPawnMovesCapture.get(attackedSquare)) != 0)
                return true;
        } else {
            if ((pieceBitboards[RivalConstants.BN] & Bitboards.knightMoves.get(attackedSquare)) != 0 ||
                    (pieceBitboards[RivalConstants.BK] & Bitboards.kingMoves.get(attackedSquare)) != 0 ||
                    (pieceBitboards[RivalConstants.BP] & Bitboards.whitePawnMovesCapture.get(attackedSquare)) != 0)
                return true;
        }

        int pieceSquare;

        long bitboardBishop =
                isWhiteAttacking ? pieceBitboards[RivalConstants.WB] | pieceBitboards[RivalConstants.WQ] :
                        pieceBitboards[RivalConstants.BB] | pieceBitboards[RivalConstants.BQ];

        while (bitboardBishop != 0) {
            bitboardBishop ^= (1L << (pieceSquare = Long.numberOfTrailingZeros(bitboardBishop)));
            if ((Bitboards.magicBitboards.magicMovesBishop[pieceSquare][(int) (((pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskBishop[pieceSquare]) * MagicBitboards.magicNumberBishop[pieceSquare]) >>> MagicBitboards.magicNumberShiftsBishop[pieceSquare])] & (1L << attackedSquare)) != 0)
                return true;
        }

        long bitboardRook =
                isWhiteAttacking ? pieceBitboards[RivalConstants.WR] | pieceBitboards[RivalConstants.WQ] :
                        pieceBitboards[RivalConstants.BR] | pieceBitboards[RivalConstants.BQ];

        while (bitboardRook != 0) {
            bitboardRook ^= (1L << (pieceSquare = Long.numberOfTrailingZeros(bitboardRook)));
            if ((Bitboards.magicBitboards.magicMovesRook[pieceSquare][(int) (((pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskRook[pieceSquare]) * MagicBitboards.magicNumberRook[pieceSquare]) >>> MagicBitboards.magicNumberShiftsRook[pieceSquare])] & (1L << attackedSquare)) != 0)
                return true;
        }

        return false;
    }

    private void addPossiblePromotionMoves(final int fromSquareMoveMask, long bitboard, boolean queenCapturesOnly) {
        int toSquare;

        while (bitboard != 0) {
            bitboard ^= (1L << (toSquare = Long.numberOfTrailingZeros(bitboard)));

            if (toSquare >= 56 || toSquare <= 7) {
                this.m_legalMoves[this.m_numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN);
                if (!queenCapturesOnly) {
                    this.m_legalMoves[this.m_numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT);
                    this.m_legalMoves[this.m_numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK);
                    this.m_legalMoves[this.m_numLegalMoves++] = fromSquareMoveMask | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP);
                }
            } else {
                this.m_legalMoves[this.m_numLegalMoves++] = fromSquareMoveMask | toSquare;
            }
        }
    }


    private void addMoves(int fromSquareMask, long bitboard) {
        int toSquare;

        while (bitboard != 0) {
            bitboard ^= (1L << (toSquare = Long.numberOfTrailingZeros(bitboard)));
            this.m_legalMoves[this.m_numLegalMoves++] = fromSquareMask | toSquare;
        }
    }

    public void generateLegalMoves() {

        clearLegalMovesArray();

        generateKnightMoves(this.m_isWhiteToMove ? pieceBitboards[RivalConstants.WN] : pieceBitboards[RivalConstants.BN]);

        generateKingMoves(this.m_isWhiteToMove ? this.m_whiteKingSquare : this.m_blackKingSquare);

        generatePawnMoves(this.m_isWhiteToMove ? pieceBitboards[RivalConstants.WP] : pieceBitboards[RivalConstants.BP],
                this.m_isWhiteToMove ? Bitboards.whitePawnMovesForward : Bitboards.blackPawnMovesForward,
                this.m_isWhiteToMove ? Bitboards.whitePawnMovesCapture : Bitboards.blackPawnMovesCapture);

        generateSliderMoves(RivalConstants.WR, RivalConstants.BR, Bitboards.magicBitboards.magicMovesRook, MagicBitboards.occupancyMaskRook, MagicBitboards.magicNumberRook, MagicBitboards.magicNumberShiftsRook);

        generateSliderMoves(RivalConstants.WB, RivalConstants.BB, Bitboards.magicBitboards.magicMovesBishop, MagicBitboards.occupancyMaskBishop, MagicBitboards.magicNumberBishop, MagicBitboards.magicNumberShiftsBishop);

        this.m_legalMoves[this.m_numLegalMoves] = 0;

    }

    private void clearLegalMovesArray() {
        this.m_numLegalMoves = 0;
        this.m_legalMoves[this.m_numLegalMoves] = 0;
    }

    private void generateSliderMoves(final int whitePieceConstant, final int blackPieceConstant, long[][] magicMovesRook, long[] occupancyMaskRook, long[] magicNumberRook, int[] magicNumberShiftsRook) {
        long rookBitboard;
        rookBitboard =
                this.m_isWhiteToMove ? pieceBitboards[whitePieceConstant] | pieceBitboards[RivalConstants.WQ]
                        : pieceBitboards[blackPieceConstant] | pieceBitboards[RivalConstants.BQ];

        while (rookBitboard != 0) {
            final int bitRef = Long.numberOfTrailingZeros(rookBitboard);
            rookBitboard ^= (1L << bitRef);
            addMoves(
                    bitRef << 16,
                    magicMovesRook[bitRef][(int) (((pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[bitRef]) * magicNumberRook[bitRef]) >>> magicNumberShiftsRook[bitRef])] & ~pieceBitboards[RivalConstants.FRIENDLY]);
        }
    }

    private void generatePawnMoves(long pawnBitboard, List<Long> bitboardMaskForwardPawnMoves, List<Long> bitboardMaskCapturePawnMoves) {
        int bitRef;
        long bitboardPawnMoves;
        while (pawnBitboard != 0) {
            pawnBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(pawnBitboard)));
            bitboardPawnMoves = bitboardMaskForwardPawnMoves.get(bitRef) & ~pieceBitboards[RivalConstants.ALL];

            bitboardPawnMoves = getBitboardPawnJumpMoves(bitboardPawnMoves);

            bitboardPawnMoves = getBitboardPawnCaptureMoves(bitRef, bitboardMaskCapturePawnMoves, bitboardPawnMoves);

            addPossiblePromotionMoves(bitRef << 16, bitboardPawnMoves, false);
        }
    }

    private void generateKingMoves(int kingSquare) {
        if (this.m_isWhiteToMove) {
            generateKingMoves(RivalConstants.CASTLEPRIV_WK, Bitboards.WHITEKINGSIDECASTLESQUARES, 3, false, RivalConstants.CASTLEPRIV_WQ, Bitboards.WHITEQUEENSIDECASTLESQUARES, 4);
        } else {
            generateKingMoves(RivalConstants.CASTLEPRIV_BK, Bitboards.BLACKKINGSIDECASTLESQUARES, 59, true, RivalConstants.CASTLEPRIV_BQ, Bitboards.BLACKQUEENSIDECASTLESQUARES, 60);
        }

        addMoves(kingSquare << 16, Bitboards.kingMoves.get(kingSquare) & ~pieceBitboards[RivalConstants.FRIENDLY]);
    }

    private void generateKingMoves(final int castlePriveleges, final long kingSideCastleSquares, int i, boolean b, int castleprivWq, long whitequeensidecastlesquares, int i4) {
        if ((m_castlePrivileges & castlePriveleges) != 0L && (pieceBitboards[RivalConstants.ALL] & kingSideCastleSquares) == 0L) {
            if (!isSquareAttacked(i, b) && !isSquareAttacked(i-1, b)) {
                this.m_legalMoves[this.m_numLegalMoves++] = (i << 16) | i-2;
            }
        }
        if ((m_castlePrivileges & castleprivWq) != 0L && (pieceBitboards[RivalConstants.ALL] & whitequeensidecastlesquares) == 0L) {
            if (!isSquareAttacked(i, b) && !isSquareAttacked(i4, b)) {
                this.m_legalMoves[this.m_numLegalMoves++] = (i << 16) | i4+1;
            }
        }
    }

    private void generateKnightMoves(long knightBitboard) {
        int bitRef;
        while (knightBitboard != 0) {
            knightBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(knightBitboard)));
            addMoves(bitRef << 16, Bitboards.knightMoves.get(bitRef) & ~pieceBitboards[RivalConstants.FRIENDLY]);
        }
    }

    private long getBitboardPawnCaptureMoves(int bitRef, List<Long> bitboardMaskCapturePawnMoves, long bitboardPawnMoves) {
        bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & pieceBitboards[RivalConstants.ENEMY];

        if (this.m_isWhiteToMove) {
            if ((pieceBitboards[RivalConstants.ENPASSANTSQUARE] & Bitboards.RANK_6) != 0) {
                bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & pieceBitboards[RivalConstants.ENPASSANTSQUARE];
            }
        } else {
            if ((pieceBitboards[RivalConstants.ENPASSANTSQUARE] & Bitboards.RANK_3) != 0) {
                bitboardPawnMoves |= bitboardMaskCapturePawnMoves.get(bitRef) & pieceBitboards[RivalConstants.ENPASSANTSQUARE];
            }
        }
        return bitboardPawnMoves;
    }

    private long getBitboardPawnJumpMoves(long bitboardPawnMoves) {
        long bitboardJumpMoves;

        if (this.m_isWhiteToMove) {
            bitboardJumpMoves = bitboardPawnMoves << 8L; // if we can move one, maybe we can move two
            bitboardJumpMoves &= Bitboards.RANK_4; // only counts if move is to fourth rank
        } else {
            bitboardJumpMoves = bitboardPawnMoves >> 8L;
            bitboardJumpMoves &= Bitboards.RANK_5;
        }
        bitboardJumpMoves &= ~pieceBitboards[RivalConstants.ALL]; // only if square empty
        bitboardPawnMoves |= bitboardJumpMoves;

        return bitboardPawnMoves;
    }

    public void generateLegalQuiesceMoves(boolean includeChecks) {

        final long possibleDestinations = pieceBitboards[RivalConstants.ENEMY];

        clearLegalMovesArray();

        final int kingSquare = this.m_isWhiteToMove ? this.m_whiteKingSquare : this.m_blackKingSquare;
        final int enemyKingSquare = this.m_isWhiteToMove ? this.m_blackKingSquare : this.m_whiteKingSquare;

        generateQuiesceKnightMoves(includeChecks,
                enemyKingSquare,
                this.m_isWhiteToMove ? pieceBitboards[RivalConstants.WN] : pieceBitboards[RivalConstants.BN]);

        addMoves(kingSquare << 16, Bitboards.kingMoves.get(kingSquare) & possibleDestinations);

        generateQuiescePawnMoves(includeChecks,
                this.m_isWhiteToMove ? Bitboards.whitePawnMovesForward : Bitboards.blackPawnMovesForward,
                this.m_isWhiteToMove ? Bitboards.whitePawnMovesCapture : Bitboards.blackPawnMovesCapture,
                enemyKingSquare,
                this.m_isWhiteToMove ? pieceBitboards[RivalConstants.WP] : pieceBitboards[RivalConstants.BP]);

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Bitboards.magicBitboards.magicMovesRook, MagicBitboards.occupancyMaskRook, MagicBitboards.magicNumberRook, MagicBitboards.magicNumberShiftsRook, RivalConstants.WR, RivalConstants.BR);

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Bitboards.magicBitboards.magicMovesBishop, MagicBitboards.occupancyMaskBishop, MagicBitboards.magicNumberBishop, MagicBitboards.magicNumberShiftsBishop, RivalConstants.WB, RivalConstants.BB);

        this.m_legalMoves[this.m_numLegalMoves] = 0;
    }

    private void generateQuiesceSliderMoves(boolean includeChecks, int enemyKingSquare, long[][] magicMovesRook, long[] occupancyMaskRook, long[] magicNumberRook, int[] magicNumberShiftsRook, final int whiteSliderConstant, final int blackSliderConstant) {
        long rookBitboard;
        int bitRef;
        long rookCheckSquares = magicMovesRook[enemyKingSquare][(int) (((pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[enemyKingSquare]) * magicNumberRook[enemyKingSquare]) >>> magicNumberShiftsRook[enemyKingSquare])];

        rookBitboard =
                this.m_isWhiteToMove ? pieceBitboards[whiteSliderConstant] | pieceBitboards[RivalConstants.WQ]
                        : pieceBitboards[blackSliderConstant] | pieceBitboards[RivalConstants.BQ];

        while (rookBitboard != 0) {
            rookBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(rookBitboard)));
            long rookMoves = magicMovesRook[bitRef][(int) (((pieceBitboards[RivalConstants.ALL] & occupancyMaskRook[bitRef]) * magicNumberRook[bitRef]) >>> magicNumberShiftsRook[bitRef])] & ~pieceBitboards[RivalConstants.FRIENDLY];
            if (includeChecks) {
                addMoves(bitRef << 16, rookMoves & (rookCheckSquares | pieceBitboards[RivalConstants.ENEMY]));
            } else {
                addMoves(bitRef << 16, rookMoves & pieceBitboards[RivalConstants.ENEMY]);
            }
        }
    }

    private void generateQuiescePawnMoves(boolean includeChecks, List<Long> bitboardMaskForwardPawnMoves, List<Long> bitboardMaskCapturePawnMoves, int enemyKingSquare, long pawnBitboard) {
        int bitRef;
        long bitboardPawnMoves;
        while (pawnBitboard != 0) {
            pawnBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(pawnBitboard)));

            bitboardPawnMoves = 0;

            if (includeChecks) {
                bitboardPawnMoves = bitboardMaskForwardPawnMoves.get(bitRef) & ~pieceBitboards[RivalConstants.ALL];

                bitboardPawnMoves = getBitboardPawnJumpMoves(bitboardPawnMoves);

                if (this.m_isWhiteToMove) {
                    bitboardPawnMoves &= Bitboards.blackPawnMovesCapture.get(enemyKingSquare);
                } else {
                    bitboardPawnMoves &= Bitboards.whitePawnMovesCapture.get(enemyKingSquare);
                }
            }

            // promotions
            bitboardPawnMoves |= (bitboardMaskForwardPawnMoves.get(bitRef) & ~pieceBitboards[RivalConstants.ALL]) & (Bitboards.RANK_1 | Bitboards.RANK_8);

            bitboardPawnMoves = getBitboardPawnCaptureMoves(bitRef, bitboardMaskCapturePawnMoves, bitboardPawnMoves);

            addPossiblePromotionMoves(bitRef << 16, bitboardPawnMoves, true);
        }
    }

    private void generateQuiesceKnightMoves(boolean includeChecks, int enemyKingSquare, long knightBitboard) {
        int bitRef;
        long possibleDestinations;
        while (knightBitboard != 0) {
            knightBitboard ^= (1L << (bitRef = Long.numberOfTrailingZeros(knightBitboard)));
            if (includeChecks) {
                possibleDestinations = pieceBitboards[RivalConstants.ENEMY] | (Bitboards.knightMoves.get(enemyKingSquare) & ~pieceBitboards[RivalConstants.FRIENDLY]);
            } else {
                possibleDestinations = pieceBitboards[RivalConstants.ENEMY];
            }
            addMoves(bitRef << 16, Bitboards.knightMoves.get(bitRef) & possibleDestinations);
        }
    }

    public void setLegalQuiesceMoves(int[] moveArray, boolean includeChecks) {
        this.m_legalMoves = moveArray;
        generateLegalQuiesceMoves(includeChecks);
    }

    public void setLegalMoves(int[] moveArray) {
        this.m_legalMoves = moveArray;
        generateLegalMoves();
    }

    public void setBitboards(Board board) {
        byte bitNum;
        long bitSet;
        int pieceIndex;
        char piece;

        whitePieceValues = 0;
        whitePawnValues = 0;
        blackPieceValues = 0;
        blackPawnValues = 0;

        this.pieceBitboards = new long[RivalConstants.NUM_BITBOARDS];

        for (int i = 0; i < 64; i++) {
            squareContents[i] = -1;
        }

        for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
            pieceBitboards[i] = 0;
            pieceSquareValues[i] = 0;
            pieceSquareValuesEndGame[i] = 0;
        }

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                bitNum = (byte) (63 - (8 * y) - x);
                bitSet = 1L << bitNum;
                piece = board.getPieceCode(x, y);
                switch (piece) {
                    case 'P':
                        squareContents[bitNum] = RivalConstants.WP;
                        pieceIndex = RivalConstants.WP;
                        whitePawnValues += Piece.PAWN.getValue();
                        break;
                    case 'p':
                        squareContents[bitNum] = RivalConstants.BP;
                        pieceIndex = RivalConstants.BP;
                        blackPawnValues += Piece.PAWN.getValue();
                        break;
                    case 'N':
                        squareContents[bitNum] = RivalConstants.WN;
                        pieceIndex = RivalConstants.WN;
                        whitePieceValues += Piece.KNIGHT.getValue();
                        break;
                    case 'n':
                        squareContents[bitNum] = RivalConstants.BN;
                        pieceIndex = RivalConstants.BN;
                        blackPieceValues += Piece.KNIGHT.getValue();
                        break;
                    case 'B':
                        squareContents[bitNum] = RivalConstants.WB;
                        pieceIndex = RivalConstants.WB;
                        whitePieceValues += Piece.BISHOP.getValue();
                        break;
                    case 'b':
                        squareContents[bitNum] = RivalConstants.BB;
                        pieceIndex = RivalConstants.BB;
                        blackPieceValues += Piece.BISHOP.getValue();
                        break;
                    case 'R':
                        squareContents[bitNum] = RivalConstants.WR;
                        pieceIndex = RivalConstants.WR;
                        whitePieceValues += Piece.ROOK.getValue();
                        break;
                    case 'r':
                        squareContents[bitNum] = RivalConstants.BR;
                        pieceIndex = RivalConstants.BR;
                        blackPieceValues += Piece.ROOK.getValue();
                        break;
                    case 'Q':
                        squareContents[bitNum] = RivalConstants.WQ;
                        pieceIndex = RivalConstants.WQ;
                        whitePieceValues += Piece.QUEEN.getValue();
                        break;
                    case 'q':
                        squareContents[bitNum] = RivalConstants.BQ;
                        pieceIndex = RivalConstants.BQ;
                        blackPieceValues += Piece.QUEEN.getValue();
                        break;
                    case 'K':
                        squareContents[bitNum] = RivalConstants.WK;
                        pieceIndex = RivalConstants.WK;
                        this.m_whiteKingSquare = bitNum;
                        break;
                    case 'k':
                        squareContents[bitNum] = RivalConstants.BK;
                        pieceIndex = RivalConstants.BK;
                        this.m_blackKingSquare = bitNum;
                        break;
                    default:
                        pieceIndex = -1;
                }
                if (pieceIndex != -1) {
                    pieceBitboards[pieceIndex] = pieceBitboards[pieceIndex] | bitSet;
                }
            }
        }

        this.m_isWhiteToMove = board.isWhiteToMove();

        int ep = board.getEnPassantFile();
        if (ep == -1) {
            pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 0;
        } else {
            if (board.isWhiteToMove()) {
                pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 1L << (40 + (7 - ep));
            } else {
                pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 1L << (16 + (7 - ep));
            }
        }

        this.m_castlePrivileges = 0;
        this.m_castlePrivileges |= (board.isWhiteKingSideCastleAvailable() ? RivalConstants.CASTLEPRIV_WK : 0);
        this.m_castlePrivileges |= (board.isWhiteQueenSideCastleAvailable() ? RivalConstants.CASTLEPRIV_WQ : 0);
        this.m_castlePrivileges |= (board.isBlackKingSideCastleAvailable() ? RivalConstants.CASTLEPRIV_BK : 0);
        this.m_castlePrivileges |= (board.isBlackQueenSideCastleAvailable() ? RivalConstants.CASTLEPRIV_BQ : 0);

        calculateSupplementaryBitboards();
    }

    public void calculateSupplementaryBitboards() {
        if (this.m_isWhiteToMove) {
            pieceBitboards[RivalConstants.FRIENDLY] =
                    pieceBitboards[RivalConstants.WP] | pieceBitboards[RivalConstants.WN] |
                            pieceBitboards[RivalConstants.WB] | pieceBitboards[RivalConstants.WQ] |
                            pieceBitboards[RivalConstants.WK] | pieceBitboards[RivalConstants.WR];

            pieceBitboards[RivalConstants.ENEMY] =
                    pieceBitboards[RivalConstants.BP] | pieceBitboards[RivalConstants.BN] |
                            pieceBitboards[RivalConstants.BB] | pieceBitboards[RivalConstants.BQ] |
                            pieceBitboards[RivalConstants.BK] | pieceBitboards[RivalConstants.BR];
        } else {
            pieceBitboards[RivalConstants.ENEMY] =
                    pieceBitboards[RivalConstants.WP] | pieceBitboards[RivalConstants.WN] |
                            pieceBitboards[RivalConstants.WB] | pieceBitboards[RivalConstants.WQ] |
                            pieceBitboards[RivalConstants.WK] | pieceBitboards[RivalConstants.WR];

            pieceBitboards[RivalConstants.FRIENDLY] =
                    pieceBitboards[RivalConstants.BP] | pieceBitboards[RivalConstants.BN] |
                            pieceBitboards[RivalConstants.BB] | pieceBitboards[RivalConstants.BQ] |
                            pieceBitboards[RivalConstants.BK] | pieceBitboards[RivalConstants.BR];
        }

        this.pieceBitboards[RivalConstants.ALL] = pieceBitboards[RivalConstants.FRIENDLY] | pieceBitboards[RivalConstants.ENEMY];
    }

    public boolean isNotOnNullMove() {
        return !this.m_isOnNullMove;
    }

    public void makeNullMove() {
        this.m_isWhiteToMove = !this.m_isWhiteToMove;

        long t = this.pieceBitboards[RivalConstants.FRIENDLY];
        this.pieceBitboards[RivalConstants.FRIENDLY] = this.pieceBitboards[RivalConstants.ENEMY];
        this.pieceBitboards[RivalConstants.ENEMY] = t;

        this.hashValue ^= EngineChessBoard.m_hashSwitchMovers;

        this.m_isOnNullMove = true;
    }

    public void unMakeNullMove() {
        makeNullMove();
        this.m_isOnNullMove = false;
    }

    public boolean makeMove(int compactMove) {
        final byte moveFrom = (byte) (compactMove >>> 16);
        final byte moveTo = (byte) (compactMove & 63);

        final long fromMask = 1L << moveFrom;
        final long toMask = 1L << moveTo;

        final int capturePiece = this.squareContents[moveTo];
        final int movePiece = this.squareContents[moveFrom];

        this.m_moveList[this.m_movesMade].capturePiece = -1;
        this.m_moveList[this.m_movesMade].move = compactMove;
        this.m_moveList[this.m_movesMade].hashValue = this.hashValue;
        this.m_moveList[this.m_movesMade].isOnNullMove = this.m_isOnNullMove;
        this.m_moveList[this.m_movesMade].pawnHashValue = this.pawnHashValue;
        this.m_moveList[this.m_movesMade].halfMoveCount = (byte) this.m_halfMoveCount;
        this.m_moveList[this.m_movesMade].enPassantBitboard = this.pieceBitboards[RivalConstants.ENPASSANTSQUARE];
        this.m_moveList[this.m_movesMade].castlePrivileges = (byte) this.m_castlePrivileges;

        this.m_isOnNullMove = false;

        this.m_halfMoveCount++;

        this.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 0;

        this.m_moveList[this.m_movesMade].movePiece = (byte) movePiece;
        this.pieceBitboards[movePiece] ^= fromMask | toMask;

        this.squareContents[moveFrom] = -1;
        this.squareContents[moveTo] = (byte) movePiece;

        this.hashValue ^= EngineChessBoard.m_pieceHashValues[movePiece][moveFrom] ^ EngineChessBoard.m_pieceHashValues[movePiece][moveTo];

        if (this.m_isWhiteToMove) {
            if (movePiece == RivalConstants.WP) {
                this.m_halfMoveCount = 0;
                this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WP][moveFrom] ^ EngineChessBoard.m_pieceHashValues[RivalConstants.WP][moveTo];

                if ((toMask & Bitboards.RANK_4) != 0 && (fromMask & Bitboards.RANK_2) != 0) {
                    this.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = fromMask << 8L;
                } else if (toMask == this.m_moveList[this.m_movesMade].enPassantBitboard) {
                    this.pieceBitboards[RivalConstants.BP] ^= toMask >>> 8;
                    blackPawnValues -= Piece.PAWN.getValue();
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BP][moveTo - 8];
                    this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BP][moveTo - 8];
                    this.m_moveList[this.m_movesMade].capturePiece = RivalConstants.BP;
                    this.squareContents[moveTo - 8] = -1;
                } else if ((compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) != 0) {
                    switch (compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                            whitePieceValues += Piece.QUEEN.getValue();
                            this.pieceBitboards[RivalConstants.WQ] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WQ][moveTo];
                            this.squareContents[moveTo] = RivalConstants.WQ;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                            whitePieceValues += Piece.ROOK.getValue();
                            this.pieceBitboards[RivalConstants.WR] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WR][moveTo];
                            this.squareContents[moveTo] = RivalConstants.WR;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                            whitePieceValues += Piece.KNIGHT.getValue();
                            this.pieceBitboards[RivalConstants.WN] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WN][moveTo];
                            this.squareContents[moveTo] = RivalConstants.WN;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                            whitePieceValues += Piece.BISHOP.getValue();
                            this.pieceBitboards[RivalConstants.WB] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WB][moveTo];
                            this.squareContents[moveTo] = RivalConstants.WB;
                            break;
                    }
                    whitePawnValues -= Piece.PAWN.getValue();
                    this.pieceBitboards[RivalConstants.WP] ^= toMask;
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WP][moveTo];
                    this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WP][moveTo];
                }
            } else if (movePiece == RivalConstants.WR) {
                if (moveFrom == Bitboards.A1) this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_WQ;
                else if (moveFrom == Bitboards.H1) this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_WK;
            } else if (movePiece == RivalConstants.WK) {
                this.m_whiteKingSquare = moveTo;
                this.m_castlePrivileges &= RivalConstants.CASTLEPRIV_WNONE;
                if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
                    this.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEKINGSIDECASTLEROOKMOVE;
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WR][Bitboards.H1] ^ EngineChessBoard.m_pieceHashValues[RivalConstants.WR][Bitboards.F1];
                    this.squareContents[Bitboards.H1] = -1;
                    this.squareContents[Bitboards.F1] = RivalConstants.WR;

                } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
                    this.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEQUEENSIDECASTLEROOKMOVE;
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WR][Bitboards.D1] ^ EngineChessBoard.m_pieceHashValues[RivalConstants.WR][Bitboards.A1];
                    this.squareContents[Bitboards.A1] = -1;
                    this.squareContents[Bitboards.D1] = RivalConstants.WR;
                }
            }

            if (capturePiece >= RivalConstants.BP) {
                this.m_moveList[this.m_movesMade].capturePiece = (byte) capturePiece;
                this.m_halfMoveCount = 0;
                this.pieceBitboards[capturePiece] ^= toMask;
                this.hashValue ^= EngineChessBoard.m_pieceHashValues[capturePiece][moveTo];

                if (capturePiece == RivalConstants.BP) {
                    blackPawnValues -= Piece.PAWN.getValue();
                    this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[capturePiece][moveTo];
                } else {
                    blackPieceValues -= RivalConstants.PIECE_VALUES.get(capturePiece);
                    if (capturePiece == RivalConstants.BR) {
                        if (toMask == Bitboards.BLACKKINGSIDEROOKMASK)
                            this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_BK;
                        else if (toMask == Bitboards.BLACKQUEENSIDEROOKMASK)
                            this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_BQ;
                    }
                }
            }
        } else {
            if (movePiece == RivalConstants.BP) {
                this.m_halfMoveCount = 0;
                this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BP][moveFrom] ^ EngineChessBoard.m_pieceHashValues[RivalConstants.BP][moveTo];

                if ((toMask & Bitboards.RANK_5) != 0 && (fromMask & Bitboards.RANK_7) != 0) {
                    this.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = toMask << 8L;
                } else if (toMask == this.m_moveList[this.m_movesMade].enPassantBitboard) {
                    whitePawnValues -= Piece.PAWN.getValue();
                    this.pieceBitboards[RivalConstants.WP] ^= toMask << 8;
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WP][moveTo + 8];
                    this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.WP][moveTo + 8];
                    this.m_moveList[this.m_movesMade].capturePiece = RivalConstants.WP;
                    this.squareContents[moveTo + 8] = -1;
                } else if ((compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) != 0) {
                    blackPawnValues -= Piece.PAWN.getValue();
                    switch (compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                            blackPieceValues += Piece.QUEEN.getValue();
                            this.pieceBitboards[RivalConstants.BQ] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BQ][moveTo];
                            this.squareContents[moveTo] = RivalConstants.BQ;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                            blackPieceValues += Piece.ROOK.getValue();
                            this.pieceBitboards[RivalConstants.BR] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BR][moveTo];
                            this.squareContents[moveTo] = RivalConstants.BR;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                            blackPieceValues += Piece.KNIGHT.getValue();
                            this.pieceBitboards[RivalConstants.BN] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BN][moveTo];
                            this.squareContents[moveTo] = RivalConstants.BN;
                            break;
                        case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                            blackPieceValues += Piece.BISHOP.getValue();
                            this.pieceBitboards[RivalConstants.BB] |= toMask;
                            this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BB][moveTo];
                            this.squareContents[moveTo] = RivalConstants.BB;
                            break;
                    }
                    this.pieceBitboards[RivalConstants.BP] ^= toMask;
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BP][moveTo];
                    this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BP][moveTo];
                }
            } else if (movePiece == RivalConstants.BR) {
                if (moveFrom == Bitboards.A8) this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_BQ;
                else if (moveFrom == Bitboards.H8) this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_BK;
            } else if (movePiece == RivalConstants.BK) {
                this.m_castlePrivileges &= RivalConstants.CASTLEPRIV_BNONE;
                this.m_blackKingSquare = moveTo;
                if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
                    this.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKKINGSIDECASTLEROOKMOVE;
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BR][Bitboards.H8] ^ EngineChessBoard.m_pieceHashValues[RivalConstants.BR][Bitboards.F8];
                    this.squareContents[Bitboards.H8] = -1;
                    this.squareContents[Bitboards.F8] = RivalConstants.BR;
                } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
                    this.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKQUEENSIDECASTLEROOKMOVE;
                    this.hashValue ^= EngineChessBoard.m_pieceHashValues[RivalConstants.BR][Bitboards.A8] ^ EngineChessBoard.m_pieceHashValues[RivalConstants.BR][Bitboards.D8];
                    this.squareContents[Bitboards.A8] = -1;
                    this.squareContents[Bitboards.D8] = RivalConstants.BR;
                }
            }

            if (capturePiece != -1) {
                this.m_moveList[this.m_movesMade].capturePiece = (byte) capturePiece;

                this.m_halfMoveCount = 0;
                this.pieceBitboards[capturePiece] ^= toMask;
                this.hashValue ^= EngineChessBoard.m_pieceHashValues[capturePiece][moveTo];
                if (capturePiece == RivalConstants.WP) {
                    whitePawnValues -= Piece.PAWN.getValue();
                    this.pawnHashValue ^= EngineChessBoard.m_pieceHashValues[capturePiece][moveTo];
                } else {
                    whitePieceValues -= RivalConstants.PIECE_VALUES.get(capturePiece);
                    if (capturePiece == RivalConstants.WR) {
                        if (toMask == Bitboards.WHITEKINGSIDEROOKMASK)
                            this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_WK;
                        else if (toMask == Bitboards.WHITEQUEENSIDEROOKMASK)
                            this.m_castlePrivileges &= ~RivalConstants.CASTLEPRIV_WQ;
                    }
                }
            }
        }

        this.m_isWhiteToMove = !this.m_isWhiteToMove;
        this.hashValue ^= m_hashSwitchMovers;
        this.m_movesMade++;

        calculateSupplementaryBitboards();

        if (isNonMoverInCheck()) {
            unMakeMove();
            return false;
        } else {
            return true;
        }
    }

    public void unMakeMove() {
        this.m_movesMade--;

        this.m_halfMoveCount = this.m_moveList[this.m_movesMade].halfMoveCount;
        this.m_isWhiteToMove = !this.m_isWhiteToMove;
        this.pieceBitboards[RivalConstants.ENPASSANTSQUARE] = this.m_moveList[this.m_movesMade].enPassantBitboard;
        this.m_castlePrivileges = this.m_moveList[this.m_movesMade].castlePrivileges;
        this.hashValue = this.m_moveList[this.m_movesMade].hashValue;
        this.pawnHashValue = this.m_moveList[this.m_movesMade].pawnHashValue;
        this.m_isOnNullMove = this.m_moveList[this.m_movesMade].isOnNullMove;

        final int fromSquare = (this.m_moveList[this.m_movesMade].move >>> 16) & 63;
        final int toSquare = this.m_moveList[this.m_movesMade].move & 63;
        final int flippedFromSquare = Bitboards.bitFlippedHorizontalAxis.get(fromSquare);
        final int flippedToSquare = Bitboards.bitFlippedHorizontalAxis.get(toSquare);
        final long fromMask = (1L << fromSquare);
        final long toMask = (1L << toSquare);

        this.squareContents[fromSquare] = this.m_moveList[this.m_movesMade].movePiece;
        this.squareContents[toSquare] = -1;

        // deal with en passants first, they are special moves and capture moves, so just get them out of the way
        if (!unMakeEnPassants(fromSquare, toSquare, flippedFromSquare, flippedToSquare, fromMask, toMask)) {

            // put capture piece back on toSquare, we don't get here if an en passant has just been unmade
            replaceCapturedPiece(toSquare, toMask);

            // for promotions, remove promotion piece from toSquare
            if (!removePromotionPiece(fromSquare, toSquare, flippedFromSquare, flippedToSquare, fromMask, toMask)) {

                // now that promotions are out of the way, we can remove the moving piece from toSquare and put it back on fromSquare
                final byte movePiece = replaceMovedPiece(fromSquare, toSquare, fromMask, toMask);

                // for castles, replace the rook
                replaceCastledRook(fromMask, toMask, movePiece);
            }
        }

        calculateSupplementaryBitboards();
    }

    private byte replaceMovedPiece(int fromSquare, int toSquare, long fromMask, long toMask) {
        final byte movePiece = this.m_moveList[this.m_movesMade].movePiece;
        this.pieceBitboards[movePiece] ^= toMask | fromMask;
        if (movePiece == RivalConstants.WK) this.m_whiteKingSquare = (byte) fromSquare;
        else if (movePiece == RivalConstants.BK) this.m_blackKingSquare = (byte) fromSquare;

        return movePiece;
    }

    private void replaceCastledRook(long fromMask, long toMask, byte movePiece) {
        if (movePiece == RivalConstants.WK) {
            if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK) {
                this.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEKINGSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.H1] = RivalConstants.WR;
                this.squareContents[Bitboards.F1] = -1;
            } else if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK) {
                this.pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEQUEENSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.A1] = RivalConstants.WR;
                this.squareContents[Bitboards.D1] = -1;

            }
        } else if (movePiece == RivalConstants.BK) {
            if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK) {
                this.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKKINGSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.H8] = RivalConstants.BR;
                this.squareContents[Bitboards.F8] = -1;

            } else if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK) {
                this.pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKQUEENSIDECASTLEROOKMOVE;
                this.squareContents[Bitboards.A8] = RivalConstants.BR;
                this.squareContents[Bitboards.D8] = -1;

            }
        }
    }

    private boolean removePromotionPiece(int fromSquare, int toSquare, int flippedFromSquare, int flippedToSquare, long fromMask, long toMask) {
        boolean done;
        final int promotionPiece = this.m_moveList[this.m_movesMade].move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL;
        if (promotionPiece != 0) {
            if (this.m_isWhiteToMove) {
                this.pieceBitboards[RivalConstants.WP] ^= fromMask;
                this.whitePawnValues += Piece.PAWN.getValue();

                switch (promotionPiece) {
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:

                        this.pieceBitboards[RivalConstants.WQ] ^= toMask;
                        this.whitePieceValues -= Piece.QUEEN.getValue();
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:

                        this.pieceBitboards[RivalConstants.WB] ^= toMask;
                        this.whitePieceValues -= Piece.BISHOP.getValue();
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:

                        this.pieceBitboards[RivalConstants.WN] ^= toMask;
                        this.whitePieceValues -= Piece.KNIGHT.getValue();
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:

                        this.pieceBitboards[RivalConstants.WR] ^= toMask;
                        this.whitePieceValues -= Piece.ROOK.getValue();
                        break;
                }
            } else {
                this.pieceBitboards[RivalConstants.BP] ^= fromMask;
                this.blackPawnValues += Piece.PAWN.getValue();

                switch (promotionPiece) {
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:

                        this.pieceBitboards[RivalConstants.BQ] ^= toMask;
                        this.blackPieceValues -= Piece.QUEEN.getValue();
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:

                        this.pieceBitboards[RivalConstants.BB] ^= toMask;
                        this.blackPieceValues -= Piece.BISHOP.getValue();
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:

                        this.pieceBitboards[RivalConstants.BN] ^= toMask;
                        this.blackPieceValues -= Piece.KNIGHT.getValue();
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:

                        this.pieceBitboards[RivalConstants.BR] ^= toMask;
                        this.blackPieceValues -= Piece.ROOK.getValue();
                        break;
                }
            }
            return true;
        }
        return false;
    }


    public int getWhitePieceValues() {
        return
            Long.bitCount(pieceBitboards[SquareOccupant.WN.getIndex()]) * Piece.KNIGHT.getValue() +
            Long.bitCount(pieceBitboards[SquareOccupant.WR.getIndex()]) * Piece.ROOK.getValue() +
            Long.bitCount(pieceBitboards[SquareOccupant.WB.getIndex()]) * Piece.BISHOP.getValue() +
            Long.bitCount(pieceBitboards[SquareOccupant.WQ.getIndex()]) * Piece.QUEEN.getValue();

    }

    public int getBlackPieceValues() {
        return
            Long.bitCount(pieceBitboards[SquareOccupant.BN.getIndex()]) * Piece.KNIGHT.getValue() +
            Long.bitCount(pieceBitboards[SquareOccupant.BR.getIndex()]) * Piece.ROOK.getValue() +
            Long.bitCount(pieceBitboards[SquareOccupant.BB.getIndex()]) * Piece.BISHOP.getValue() +
            Long.bitCount(pieceBitboards[SquareOccupant.BQ.getIndex()]) * Piece.QUEEN.getValue();

    }

    public int getWhitePawnValues() {
        return Long.bitCount(pieceBitboards[SquareOccupant.WP.getIndex()]) * Piece.PAWN.getValue();

    }

    public int getBlackPawnValues() {
        return Long.bitCount(pieceBitboards[SquareOccupant.BP.getIndex()]) * Piece.PAWN.getValue();
    }

    private void replaceCapturedPiece(int toSquare, long toMask) {
        final byte capturePiece = this.m_moveList[this.m_movesMade].capturePiece;
        if (capturePiece != -1) {
            this.squareContents[toSquare] = capturePiece;

            this.pieceBitboards[capturePiece] ^= toMask;

            if (capturePiece == RivalConstants.WP) this.whitePawnValues += Piece.PAWN.getValue();
            else if (capturePiece == RivalConstants.BP) this.blackPawnValues += Piece.PAWN.getValue();
            else if (capturePiece <= RivalConstants.WR)
                this.whitePieceValues += RivalConstants.PIECE_VALUES.get(capturePiece);
            else
                this.blackPieceValues += RivalConstants.PIECE_VALUES.get(capturePiece);
        }
    }

    private boolean unMakeEnPassants(int fromSquare, int toSquare, int flippedFromSquare, int flippedToSquare, long fromMask, long toMask) {
        if (toMask == this.m_moveList[this.m_movesMade].enPassantBitboard) {
            if (this.m_moveList[this.m_movesMade].movePiece == RivalConstants.WP) {
                this.pieceBitboards[RivalConstants.WP] ^= toMask | fromMask;
                this.pieceBitboards[RivalConstants.BP] ^= toMask >>> 8;
                this.blackPawnValues += Piece.PAWN.getValue();
                this.squareContents[toSquare - 8] = RivalConstants.BP;


                return true;
            } else if (this.m_moveList[this.m_movesMade].movePiece == RivalConstants.BP) {
                this.pieceBitboards[RivalConstants.BP] ^= toMask | fromMask;
                this.pieceBitboards[RivalConstants.WP] ^= toMask << 8;
                this.whitePawnValues += Piece.PAWN.getValue();
                this.squareContents[toSquare + 8] = RivalConstants.WP;


                return true;
            }
        }
        return false;
    }

    public int lastCapturePiece() {
        return this.m_moveList[this.m_movesMade - 1].capturePiece;
    }

    public boolean wasCapture() {
        return this.m_moveList[this.m_movesMade - 1].capturePiece == -1;
    }

    public boolean wasPawnPush() {
        int toSquare = this.m_moveList[this.m_movesMade - 1].move & 63;
        int movePiece = this.m_moveList[this.m_movesMade - 1].movePiece;

        if (movePiece % 6 != RivalConstants.WP) return false;

        if (toSquare >= 48 || toSquare <= 15) return true;

        if (!m_isWhiteToMove) // white made the last move
        {
            if (toSquare >= 40)
                return (Long.bitCount(Bitboards.whitePassedPawnMask.get(toSquare) & pieceBitboards[RivalConstants.BP]) == 0);
        } else {
            if (toSquare <= 23)
                return (Long.bitCount(Bitboards.blackPassedPawnMask.get(toSquare) & pieceBitboards[RivalConstants.WP]) == 0);
        }

        return false;
    }

    public int previousOccurrencesOfThisPosition() {
        int occurrences = 0;
        for (int i = this.m_movesMade - 2; i >= 0 && i >= this.m_movesMade - this.m_halfMoveCount; i -= 2) {
            if (this.m_moveList[i].hashValue == this.hashValue) {
                occurrences++;
            }
        }

        return occurrences;
    }

    public String getFen() {
        StringBuilder fen = new StringBuilder();
        char[] board = getCharBoard();

        char spaces = '0';
        for (int i = 63; i >= 0; i--) {
            if (board[i] == 0) {
                spaces++;
            } else {
                if (spaces > '0') {
                    fen.append(spaces);
                    spaces = '0';
                }
                fen.append(board[i]);
            }
            if (i % 8 == 0) {
                if (spaces > '0') {
                    fen.append(spaces);
                    spaces = '0';
                }
                if (i > 0) {
                    fen.append('/');
                }
            }
        }

        fen.append(' ');
        fen.append(m_isWhiteToMove ? 'w' : 'b');
        fen.append(' ');

        boolean noPrivs = true;

        if ((m_castlePrivileges & RivalConstants.CASTLEPRIV_WK) != 0) {
            fen.append('K');
            noPrivs = false;
        }
        if ((m_castlePrivileges & RivalConstants.CASTLEPRIV_WQ) != 0) {
            fen.append('Q');
            noPrivs = false;
        }
        if ((m_castlePrivileges & RivalConstants.CASTLEPRIV_BK) != 0) {
            fen.append('k');
            noPrivs = false;
        }
        if ((m_castlePrivileges & RivalConstants.CASTLEPRIV_BQ) != 0) {
            fen.append('q');
            noPrivs = false;
        }

        if (noPrivs) fen.append('-');

        fen.append(' ');
        long bitboard = pieceBitboards[RivalConstants.ENPASSANTSQUARE];
        if (Long.bitCount(bitboard) > 0) {
            int epSquare = Long.numberOfTrailingZeros(bitboard);
            char file = (char) (7 - (epSquare % 8));
            char rank = (char) (epSquare <= 23 ? 2 : 5);
            fen.append((char) (file + 'a'));
            fen.append((char) (rank + '1'));
        } else {
            fen.append('-');
        }

        return fen.toString();
    }

    private char[] getCharBoard() {
        char[] board = new char[64];
        char[] pieces = new char[]{'P', 'N', 'B', 'Q', 'K', 'R', 'p', 'n', 'b', 'q', 'k', 'r'};

        for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
            List<Integer> bitsSet = Bitboards.getSetBits(pieceBitboards[i]);
            for (int bitSet : bitsSet) {
                board[bitSet] = pieces[i];
            }
        }
        return board;
    }

    public boolean isMoveLegal(int moveToVerify) {
        moveToVerify &= 0x00FFFFFF;
        int[] moves = new int[RivalConstants.MAX_GAME_MOVES];
        this.setLegalMoves(moves);
        int i = 0;
        while (moves[i] != 0) {
            int move = moves[i] & 0x00FFFFFF;
            if (this.makeMove(move)) {
                if (move == moveToVerify) {
                    this.unMakeMove();
                    return true;
                }
                this.unMakeMove();
            }
            i++;
        }
        return false;
    }


}
